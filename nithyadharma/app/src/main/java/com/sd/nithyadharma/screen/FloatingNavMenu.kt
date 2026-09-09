package com.sd.nithyadharma.screen

import LocaleManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sd.nithyadharma.cards.CardFactory
import com.sd.nithyadharma.cards.CardMode
import com.sd.nithyadharma.cards.CardModel
import com.sd.nithyadharma.cards.CardType
import com.sd.nithyadharma.cards.toEnglish
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.AlarmSlotNotificationHelpers.addRasiPalanCard
import com.sd.nithyadharma.util.FirebaseAppAnalytics
import com.sd.nithyadharma.util.LocalAppLanguage

// --- Data Model for Menu Items ---
sealed class NavMenuItemType {
    data class Navigation(val route: String) : NavMenuItemType()
    data class CreateCard(val createCard: () -> CardModel) : NavMenuItemType()
    data class ExecuteAction(val onExecute: () -> Unit) : NavMenuItemType()
}

data class NavMenuItem(
    val title: String,
    val icon: ImageVector,
    val type: NavMenuItemType
)

// this func makes each one of the menu items of type CreateCard
fun CardType.toNavMenuItem(
    icon: ImageVector,
    currentLang: NDLanguage
): NavMenuItem {
    val cardName = this.toEnglish()

    return NavMenuItem(
        title = LocaleManager.getString(cardName, currentLang),
        icon = icon,
        type = NavMenuItemType.CreateCard {
            FirebaseAppAnalytics.logScreenView(cardName)
            CardFactory.makeCard(
                mode = CardMode.WRITE_FG,
                type = this, // Using overloaded CardFactory.makeCard(type)
                expiryOffsetMillis = CardFactory.CARD_EXPIRY_OFFSET_MEDIUM
            )
        }
    )
}

@Composable
fun FloatingNavMenu(
    onNavigate: (String) -> Unit,
    onPushCard: (CardModel) -> Unit, // Callback to push cards to MainViewModel
    onAddRasiPalanCard: () -> Unit, // Callback for Rasi Palan logic
    cardColor: Color = MaterialTheme.colorScheme.surface,
    onLanguageSelected: (NDLanguage) -> Unit = {},
) {
    val currentLang = LocalAppLanguage.current

    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val targetY = (configuration.screenHeightDp * 0.75f).dp

    val adaptiveCardBackground = cardColor.copy(alpha = 0.55f)

    // Define all menu items dynamically
    val menuItems = remember(currentLang) {
        listOf(
            CardType.HINDU_CALENDAR.toNavMenuItem(Icons.Default.DateRange, currentLang),
            CardType.COUNTER.toNavMenuItem(Icons.Default.Refresh, currentLang),
            NavMenuItem(
                title = LocaleManager.getString(CardType.RASI_PALAN.toEnglish(), currentLang),
                icon = Icons.Default.Language,
                type = NavMenuItemType.ExecuteAction {
                    onAddRasiPalanCard()
                }
            ),
//            CardType.MUSIC.toNavMenuItem(Icons.Default.MusicNote, currentLang),  triggered by slot changes only
            CardType.PUJA_STORE.toNavMenuItem(Icons.Default.ShoppingCart, currentLang),
            CardType.LIGHT_A_LAMP.toNavMenuItem(Icons.Default.Star, currentLang),
            CardType.TEMPLE_NEEDS.toNavMenuItem(Icons.Default.HourglassEmpty, currentLang),
            CardType.PANCHANGAM.toNavMenuItem(Icons.Default.Settings, currentLang),
//            CardType.MAP.toNavMenuItem(Icons.Default.Settings, currentLang),
            CardType.FUTURE_N_DAYS.toNavMenuItem(Icons.Default.AddChart, currentLang)
        )
    }

    // Centralized Click Handler
    val handleItemClick: (NavMenuItem) -> Unit = { item ->
        expanded = false
        when (val type = item.type) {
            is NavMenuItemType.Navigation -> {
                onNavigate(type.route)
            }
            is NavMenuItemType.CreateCard -> {
                // for normal create stateless cards
                val card = type.createCard()
                onPushCard(card) // this calls mainViewModel.saveAndAddCard(card)
                Toast.makeText(context, "${item.title} \uD83D\uDC46\uD83C\uDFFB\uD83C\uDFF5\uFE0F!", Toast.LENGTH_SHORT).show()
            }
            is NavMenuItemType.ExecuteAction -> {
                // for executing other actions
                type.onExecute() // Executes any function/action directly
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Semi-transparent backdrop to dismiss menu on tap outside
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = false }
            )
        }

        // 2. Floating Menu Container
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight(0.75f)
                .padding(end = 12.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = adaptiveCardBackground,
                    contentColor = contentColorFor(cardColor),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .widthIn(max = 240.dp)
                        .heightIn(max = 470.dp) // this is where the height is set
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        LanguageMenuItem(
                            selectedLang = currentLang,
                            onLangSelected = { newLang ->
                                onLanguageSelected(newLang)
                                expanded = false // Closes the menu on language selection
                            },
                            textColor = Color.White.copy(alpha = 0.8f)
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color.White.copy(alpha = 0.4f)
                        )

                        menuItems.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = Color.White
                                    )
                                },
                                onClick = { handleItemClick(item) }
                            )
                        }
                    }
                }
            }
        }

        // 3. Floating Action Button
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = targetY)
                .padding(end = 12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                onClick = { expanded = !expanded },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                shadowElevation = 6.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.Menu,
                        contentDescription = "Toggle Navigation Menu"
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageMenuItem(
    selectedLang: NDLanguage,
    onLangSelected: (NDLanguage) -> Unit,
    textColor: Color,
) {
    val toggleBg = textColor.copy(alpha = 0.15f)
    val activeColor = textColor.copy(alpha = 0.30f)

    DropdownMenuItem(
        enabled = false, // 💡 Prevents outer row ripple so individual pill taps work cleanly
        onClick = { /* Handle via sub-boxes below */ },
        colors = MenuDefaults.itemColors(
            disabledLeadingIconColor = Color.White // Keep icon white even when item is disabled
        ),
        // commented so the text takes more space
//        leadingIcon = {
//            // 💡 1. Automatically uses standard 12.dp start margin and alignment
//            Icon(
//                imageVector = Icons.Default.Language,
//                contentDescription = "Language",
//                tint = Color.White
//            )
//        },
        text = {
            // 💡 2. Toggle Pill sits in the exact same slot where text starts for other items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(toggleBg)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NDLanguage.entries.forEach { language ->
                    val isSelected = language == selectedLang

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) activeColor else Color.Transparent)
                            .clickable { onLangSelected(language) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
//                            text = if (language == NDLanguage.TA) "தமிழ்" else "English",
                            text = language.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) textColor else textColor.copy(alpha = 0.6f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    )
}