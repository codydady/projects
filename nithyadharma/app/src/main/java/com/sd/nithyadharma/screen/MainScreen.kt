package com.sd.nithyadharma.screen

import LocaleManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sd.nithyadharma.R
import com.sd.nithyadharma.cards.CardFactory
import com.sd.nithyadharma.cards.CardModel
import com.sd.nithyadharma.cards.CardType
import com.sd.nithyadharma.cards.CounterCardContent
import com.sd.nithyadharma.cards.DharmaTodayCardContent
import com.sd.nithyadharma.cards.FuturePanchangamContent
import com.sd.nithyadharma.cards.GreetingCardContent
import com.sd.nithyadharma.cards.HinduCalendarCardContent
import com.sd.nithyadharma.cards.LightALampCardContent
import com.sd.nithyadharma.cards.MusicCardContent
import com.sd.nithyadharma.cards.NaalKaattiCardContent
import com.sd.nithyadharma.cards.PanchangamCardContent
import com.sd.nithyadharma.cards.PanchangamNotificationCardContent
import com.sd.nithyadharma.cards.PujaStoreCardContent
import com.sd.nithyadharma.cards.RasiPalanCardContent
import com.sd.nithyadharma.cards.RatingShareCardContent
import com.sd.nithyadharma.cards.RequestHelpCardContent
import com.sd.nithyadharma.cards.formatCardTimestamp
import com.sd.nithyadharma.cards.formatMillisToHoursMins
import com.sd.nithyadharma.cards.toEnglish
import com.sd.nithyadharma.model.MainViewModel
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.model.PanchangaAttr
import com.sd.nithyadharma.model.PanchangaAttr.Rasi
import com.sd.nithyadharma.util.Constants
import com.sd.nithyadharma.util.FirebaseAppAnalytics
import com.sd.nithyadharma.util.LocalAppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- 2. MAIN SCREEN CONTAINER ---
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onSaveUserSetup: (String, Rasi, NDLanguage) -> Unit, // 👈 Receives event callback
    onDismissCard: (String) -> Unit,
    onTogglePinCard: (String) -> Unit,
    onSlotColorChanged: (Color) -> Unit,
    onTempleItemClick: () -> Unit
) {
    // 🔑 Read language directly from the Composition environment
    val currentLang = LocalAppLanguage.current

    val currentSlot by mainViewModel.currentSlot.collectAsState()

    val color1 = parseHexColor(currentSlot.gradientColorHex1, Color(0xFF3B0008))
    val color2 = parseHexColor(currentSlot.gradientColorHex2, Color(0xFFC0392B))
    val textColor = parseHexColor(currentSlot.textColorHex, Color.White)
    val midColor = lerp(color1, color2, 0.5f)

    // 🔑 Collect state from MainViewModel
    val activeCards by mainViewModel.activeCards.collectAsState()

    val customerInfo by mainViewModel.customerInfo.collectAsStateWithLifecycle()

    // 🔑 Read cached static & dynamic panchangam directly from ViewModel
    val staticPanchangam by mainViewModel.staticPanchangam.collectAsState()
    val dynamicPanchangam by mainViewModel.dynamicPanchangam.collectAsState()
    val futureNDayPanchangam by mainViewModel.futurePanchangam.collectAsState()

    // for post of the day if it exists
    val postOfDay by mainViewModel.postOfDay.collectAsState()

    // Prompt user if customerInfo loaded and name is still blank
    //    if (customerInfo != null && customerInfo.name.isBlank()) {
    if (customerInfo?.name?.isBlank() == true) {
        UserSetupDialog(
            currentLang = currentLang,
            slotHex1 = currentSlot.gradientColorHex1,
            slotHex2 = currentSlot.gradientColorHex2,
            onSetupSubmitted = { newName, selectedRasi, selectedLang ,->
                onSaveUserSetup(newName, selectedRasi, selectedLang) // 👈 Forwards event back up to MainActivity
            }
        )
    }

    LaunchedEffect(midColor) {
        onSlotColorChanged(midColor)
    }

    val glowProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "GlowProgress"
    )

    val listState = rememberLazyListState()

    // Automatically jump to index 0 whenever activeCards list size increases while scrolled
    LaunchedEffect(activeCards.size) {
        if (activeCards.isNotEmpty() && listState.firstVisibleItemIndex > 0) {
            kotlinx.coroutines.yield()
            listState.scrollToItem(
                index = 0,
                scrollOffset = 0
            )
        }
    }

    // ---------------------
    // rendering begins here
    // ---------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .drawBehind {
                if (glowProgress > 0f) {
                    val animatedColor1 = lerp(Color.Black, color1, glowProgress)
                    val animatedColor2 = lerp(Color.Black, color2, glowProgress)
                    drawRect(
                        brush = Brush.verticalGradient(listOf(animatedColor1, animatedColor2))
                    )
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 10 PT TOP CLEARANCE + STATIC TITLE CARD ---
        Spacer(modifier = Modifier.height(10.dp))

        TitleCard(
            mainViewModel = mainViewModel,
            onTempleItemClick,
            textColor = Constants.DarkIvory, // we override slot theme textColor,
            currentLang = currentLang,
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth(0.95f) // Matched to container width (0.95f)
        )

        // --- REDUCED SPACE BETWEEN TITLE CARD AND CONTAINER ---
        Spacer(modifier = Modifier.height(4.dp))

        // --- OUTER SCROLLABLE CONTAINER WITH ELEGANT BORDER ---
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .weight(1f)
                .border(
                    width = 0.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            textColor.copy(alpha = 0.35f),
                            textColor.copy(alpha = 0.10f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.30f))
                .padding(horizontal = 12.dp, vertical = 8.dp),   // or reduce top only
            contentAlignment = Alignment.TopCenter // 👈 1. ADD THIS to center the empty message perfectly
        ) {
            if (activeCards.isEmpty()) { // 👈 2. ADD THIS CHECK

                // 👈 3. ADD YOUR EMPTY TEXT HERE
                val shortExpiryText = formatMillisToHoursMins(CardFactory.CARD_EXPIRY_OFFSET_SHORT) // "6:00h"
                val longExpiryText = formatMillisToHoursMins(CardFactory.CARD_EXPIRY_OFFSET_LONG)   // "12:00h"

                // no need to locale this to tamil as it would be an overkill i felt
                Text(
                    text =  "Welcome, Older cards expire between " +
                            "$shortExpiryText - $longExpiryText hours when created " +
                            "depending on their nature and newer cards will automatically " +
                            "appear during various times of the day.\n\n"
//                            +
//                        "வணக்கம்! திரையில் தகவல் இல்லை. நீங்கள் உருவாக்கவில்லை அல்லது " +
//                            "பழைய தகவல்கள் $shortExpiryText - $longExpiryText மணி நேரத்தில் மறைந்துவிடும்." +
//                            "புதிய தகவல்கள் வெவ்வேறு நேரத்தில் தானாக வந்து சேரும்."
                    ,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.5f),
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 180.dp, end = 16.dp), // Positioned around ~75% height
                    horizontalAlignment = Alignment.End
                ) {
                    // Shown on background when no cards exist
                    PointerToMenuAt75Percent(
                        textColor = textColor,
                        message = "பயணத்தைத் தொடங்குங்கள்!\n\n" +
                                "Tap the menu to get started\n\n" +
                                "ಮೆನುವನ್ನು ಸ್ಪರ್ಶಿಸಿ ಅನುಭವವನ್ನು ಪಡೆಯಿರಿ\n\n" +
                                "शुरू करने के लिए मेनू टैप करें"
                    )
                }

            } // if activecards is empty ends

            else { // activecards aint empty , display them
//                Log.d("++++Mainscreen", "activecards aint empty")

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp), // 👈 Prevents top card clipping against outer Box
                    verticalArrangement = Arrangement.spacedBy(8.dp), // Spacing between stacked cards
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    /* important, this is the loop which creates the cards when it sees a new
                        update or create on te activeCards preference value as emitted by
                        mainviewmodel. but it will create a brand new card which doesnt respect
                        the content the card was created with. for eg, hindu calendar is okay as it
                        is stateless but panchangam or other text card needs the created content then.
                        this will be a challenge while app is killed and restarted.
                     */
                    items(
                        items = activeCards,
                        key = { it.id }
                    ) { card ->
//                        Log.d("++++Mainscreen", "activecards composing ${card.type}")

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(
                                    fadeInSpec = tween(
                                        durationMillis = 1500,
                                        easing = FastOutSlowInEasing
                                    ),
                                    placementSpec = tween(
                                        durationMillis = 1400,
                                        easing = FastOutSlowInEasing
                                    ),
                                    fadeOutSpec = tween(
                                        durationMillis = 600
                                    )
                                )
                        ) {
                            DismissibleCardContainer(
                                card = card,
                                textColor = textColor,
                                currentLang = currentLang,
                                onDismiss = { onDismissCard(card.id) },
                                onTogglePin = { onTogglePinCard(card.id) }
                            ){
                                /* 🔑 imp THIS IS THE CONTENT LAMBDA !
                                       set as last arg content inside this function
                                        in its definition. must be last arg.
                                 */

                                // The 'when' block lives right here inside MainScreen
                                when (card.type) {
                                    CardType.COUNTER -> {
                                        // 1. Collect values safely with lifecycle
                                        val count by mainViewModel.counterValue.collectAsStateWithLifecycle()
                                        val alertInterval by mainViewModel.alertInterval.collectAsStateWithLifecycle()
                                        val finalCount by mainViewModel.finalCount.collectAsStateWithLifecycle()
                                        // 2. Pass pure state and event callbacks down
                                        CounterCardContent(
                                            textColor = textColor,
                                            count = count,  // note this and rest could ve been in customparams but since it aint a
                                            alertInterval = alertInterval,  // big deal to maintain state in this card , im leaving it
                                            finalCount = finalCount,
                                            onCountChanged = { newCount ->
                                                mainViewModel.updateCounter(newCount)
                                            },
                                            onSettingsChanged = { interval, target ->
                                                mainViewModel.updateCounterSettings(interval, target)
                                            }
                                        )
                                    }
                                    CardType.HINDU_CALENDAR -> {
                                        HinduCalendarCardContent(
                                            textColor = textColor
                                        )
                                    }
                                    CardType.TEMPLE_NEEDS -> {
                                        // 1. Collect CustomerInfo safely from ViewModel StateFlow
//                                        val customerInfo by mainViewModel.customerInfo.collectAsStateWithLifecycle()

                                        RequestHelpCardContent(
                                            textColor = textColor,
                                            customerInfo = customerInfo
                                        )
                                    }
                                    CardType.PUJA_STORE -> {
                                        // 1. Collect CustomerInfo safely from ViewModel StateFlow
//                                        val customerInfo by mainViewModel.customerInfo.collectAsStateWithLifecycle()
                                        // 2. Render Card and delegate save callback up to ViewModel
                                        PujaStoreCardContent(
                                            textColor = textColor,
                                            customerInfo = customerInfo,
                                            onSaveCustomerInfo = { updatedCustomerInfo ->
                                                mainViewModel.saveCustomerInfo(updatedCustomerInfo)
                                            }
                                        )
                                    }
                                    CardType.LIGHT_A_LAMP -> {
                                        LightALampCardContent(
                                            textColor = textColor
                                        )
                                    }
                                    CardType.PANCHANGAM -> {
                                        /*
                                            todo , for panchangam , while loading cards , we must
                                             serialise and persist both staticPanchangam & dynamicPanchangam
                                             in customjson field of the cardmodel object and treat it
                                             with cardmode read or write param.
                                         */
                                        /*
                                            todo , not doing
                                             val panchangamData = mapOf(
                                                "staticData" to Json.encodeToJsonElement(staticPanchangam),
                                                "dynamicData" to Json.encodeToJsonElement(dynamicPanchangam)
                                                )
                                                as the sp and dp objects have no serialisers, rather its too much to implement now
                                         */
                                        PanchangamCardContent(
                                            sp = staticPanchangam,
                                            dp = dynamicPanchangam,
                                            textColor = textColor
                                        )
                                    }
                                    CardType.DP_NOTIFICATION -> {
                                        PanchangamNotificationCardContent(
                                            paramsMap = card.customParams ?: emptyMap(),
                                            textColor = textColor
                                        )
                                    }
                                    CardType.NAAL_KAATTI -> {
                                        NaalKaattiCardContent(
                                            sp = staticPanchangam,
                                            textColor = textColor
                                        )
                                    }
                                    CardType.FUTURE_N_DAYS -> {
                                        FuturePanchangamContent(
                                            nextNDaysDp = futureNDayPanchangam,
                                            textColor = textColor
                                        )
                                    }
                                    CardType.MUSIC -> {
                                        /* TODO() we dont want to do it as it is only triggered
                                         from slot changes. it must be dummy to avoid issues
                                         */
                                        MusicCardContent(
                                            paramsMap = card.customParams ?: emptyMap(),
                                            textColor = textColor
                                        )
                                    }
                                    CardType.RATING_SHARE -> {
                                        RatingShareCardContent(
                                            textColor = textColor,
                                            onRatingSelected = { rating ->
                                                FirebaseAppAnalytics.logRating(
                                                    card.type.toString(),
                                                    rating
                                                )
                                            }
                                        )
                                    }
                                    CardType.GREETING -> {
                                        GreetingCardContent(
                                            paramsMap = card.customParams ?: emptyMap(),
                                            textColor = textColor
                                        )
                                    }
                                    CardType.MAP -> TODO() // since it is big, leaving as a screen.

                                    CardType.TODAYS_DHARMA -> {
                                        postOfDay?.let { post ->
                                            DharmaTodayCardContent(
                                                post = post,
                                                textColor = textColor
                                            )
                                        }
                                    } // todays dharma ends
                                    CardType.RASI_PALAN -> {
                                        RasiPalanCardContent(
                                            paramsMap = card.customParams ?: emptyMap(),
                                            textColor = textColor
                                        )
                                    }
                                }
                            }
                        }
                    } // items for loop ends
                } // LazyColumn. ends
            } // active cards not empty ends
        } // scrollable outer box with title and dis card containers ends

        // --- 10 PT BOTTOM CLEARANCE ---
        Spacer(modifier = Modifier.height(10.dp))
    }
}

// --- 3. STATIC TITLE CARD COMPONENT ---

@Composable
fun TitleCard(
    mainViewModel: MainViewModel,
    onTempleItemClick: () -> Unit,
    textColor: Color,
    currentLang: NDLanguage,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.30f))
            .padding(horizontal = 20.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Icon on the left
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Logo on the Left (Round & Opaque)
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            // create a random song
                            //mainViewModel.testingDummy()
                            onTempleItemClick()
                        }
                        .clip(CircleShape) // Makes the logo circular
                )

                Spacer(modifier = Modifier.width(8.dp)) // Space between logo and text

                // 2. Text on the Right
                Text(
                    text = LocaleManager.getString("app_title", currentLang),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = textColor
                    ),
                    modifier = Modifier.clickable {
                        // todo temp quick place for new feature test
                        CoroutineScope(Dispatchers.IO).launch {
                            //val preferencesManager = PreferencesManager(appContext)
                            mainViewModel.testingDummyRatingCardMaker()
                        }

                    }
                )
            }

            Text(
                text = LocaleManager.getString("app_motto", currentLang),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = textColor.copy(alpha = 0.85f)
                ),
                modifier = Modifier.padding(top = 4.dp)
                            .clickable {
                            // todo temp quick place for new feature test
                            CoroutineScope(Dispatchers.IO).launch {
                                //val preferencesManager = PreferencesManager(appContext)
                                mainViewModel.testingDummyRasiPalanCardMaker()
                            }
                }
            )
        }
    }
}

@Composable
fun DismissibleCardContainer(
    card: CardModel,
    textColor: Color,
    currentLang: NDLanguage,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Format timestamp once per recomposition
    val formattedCreDttm = remember(card.createdTime) {
        formatCardTimestamp(card.createdTime)
    }

    val formattedExpDttm = remember(card.expiryTime) {
        formatCardTimestamp(card.expiryTime)
    }

    val animDuration = 1200
    val dismissDelay = (animDuration + 100).toLong()

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(1000)),
        exit = fadeOut(animationSpec = tween(animDuration)) +
                scaleOut(
                    targetScale = 0.80f,
                    animationSpec = tween(animDuration)
                ) +
                shrinkVertically(
                    animationSpec = tween(animDuration)
                )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 450.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.30f))
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                // =========================================================
                // ROW 1: Title + Like
                // =========================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = LocaleManager.getString(
                            card.type.toEnglish(),
                            currentLang
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Like button
                    var isLiked by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = {
                            isLiked = !isLiked

                            if (isLiked) {
                                FirebaseAppAnalytics.logCardLiked(
                                    card.type.toString(),
                                    card.loggingKey // which contains the current dt
                                )
                            }
                        },
                        modifier = Modifier.size(40.dp).padding(end = 20.dp)

                    ) {
                        Icon(
                            imageVector = if (isLiked) {
                                Icons.Filled.ThumbUp
                            } else {
                                Icons.Outlined.ThumbUp
                            },
                            contentDescription = "Like Card",
                            tint = if (isLiked) {
                                Color(0xFFFFD700)
                            } else {
                                Color.White.copy(alpha = 0.8f)
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } // Title + Like ends

                // =========================================================
                // ROW 2: Created/Expires + Pin + Close
                // =========================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Created / Expires
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Created: $formattedCreDttm",
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            color = textColor
                        )

                        Text(
                            text = "Expires: $formattedExpDttm",
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            color = textColor
                        )
                    }

                    // Pin + Close
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Pin
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin Card",
                            tint = if (card.isPinned) {
                                textColor
                            } else {
                                textColor.copy(alpha = 0.4f)
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .clickable {
                                    onTogglePin()
                                }
                        )

                        // Close
                        if (card.isCloseable) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        textColor.copy(alpha = 0.15f)
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            isVisible = false
                                            delay(dismissDelay)
                                            onDismiss()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = textColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } // Created/Expires + Pin + Close ends

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.White.copy(alpha = 0.3f)
                )

                // =========================================================
                // CARD INNER CONTENT
                // =========================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    content()
                }
            }
        }
    }
}

// Safe Hex Color Parser
fun parseHexColor(hexString: String, defaultColor: Color = Color.Black): Color {
    return try {
        val cleanHex = hexString
            .trim()
            .removePrefix("0x")
            .removePrefix("0X")
            .removePrefix("#")
        Color(cleanHex.toLong(16))
    } catch (e: Exception) {
        defaultColor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSetupDialog(
    slotHex1: String,
    slotHex2: String,
    currentLang: NDLanguage, // 👈 Initial language from outside
    onSetupSubmitted: (name: String, rasi: Rasi, language: NDLanguage) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var selectedRasi by remember { mutableStateOf(Rasi.MESHA) }
    var isRasiDropdownExpanded by remember { mutableStateOf(false) }

    // 🔑 Local state: keeps language changes isolated strictly to this dialog
    var selectedLang by remember { mutableStateOf(currentLang) }
    var isLanguageDropdownExpanded by remember { mutableStateOf(false) }

    val isValid = nameInput.trim().isNotBlank()

    val color1 = parseHexColor(slotHex1, Color(0xFF3B0008))
    val color2 = parseHexColor(slotHex2, Color(0xFFC0392B))

    val gradientBrush = remember(color1, color2) {
        Brush.verticalGradient(listOf(color1, color2))
    }

    Dialog(
        onDismissRequest = { /* Prevent dismiss */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(gradientBrush)
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Row with Title and Local Language Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LocaleManager.getString("welcome_title", selectedLang), // Uses local dialogLang
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // 🔑 Language Toggle Switch (e.g. English <-> Tamil)
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        Text(
//                            text = if (dialogLang == NDLanguage.EN) "Eng" else "தமிழ்",
//                            style = MaterialTheme.typography.labelLarge,
//                            color = Color.White,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Switch(
//                            checked = dialogLang == NDLanguage.TA, // Adjust enum comparison to your language enum
//                            onCheckedChange = { isTamil ->
//                                dialogLang = if (isTamil) NDLanguage.TA else NDLanguage.EN
//                            },
//                            colors = SwitchDefaults.colors(
//                                checkedThumbColor = color1,
//                                checkedTrackColor = Color.White,
//                                uncheckedThumbColor = Color.White,
//                                uncheckedTrackColor = Color.White.copy(alpha = 0.4f),
//                                uncheckedBorderColor = Color.Transparent
//                            )
//                        )
//                    }// old row ends
                    ///
                    Box {
                        OutlinedButton(
                            onClick = {
                                isLanguageDropdownExpanded = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.6f)
                            )
                        ) {
                            Text(
                                text = selectedLang.displayName,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        DropdownMenu(
                            expanded = isLanguageDropdownExpanded,
                            onDismissRequest = {
                                isLanguageDropdownExpanded = false
                            }
                        ) {
                            NDLanguage.entries.forEach { language ->

                                val isSelected = language == selectedLang

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = language.displayName,
                                            fontWeight = if (isSelected) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            }
                                        )
                                    },
                                    onClick = {
                                        selectedLang = language
                                        isLanguageDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    ////
                }

                Text(
                    text = LocaleManager.getString("welcome_subtitle", selectedLang),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )

                // 2. Name Input Field
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = {
                        Text(
                            text = LocaleManager.getString("your_name_label", selectedLang),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )

                // 3. Rasi Dropdown Selector (Uses dialogLang)
                ExposedDropdownMenuBox(
                    expanded = isRasiDropdownExpanded,
                    onExpandedChange = { isRasiDropdownExpanded = !isRasiDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        // 🔑 Displays selected Rasi using local dialogLang
                        value = PanchangaAttr.rasiName(selectedRasi, selectedLang),
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                text = LocaleManager.getString("select_rasi_label", selectedLang),
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRasiDropdownExpanded)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            focusedTrailingIconColor = Color.White,
                            unfocusedTrailingIconColor = Color.White
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isRasiDropdownExpanded,
                        onDismissRequest = { isRasiDropdownExpanded = false }
                    ) {
                        Rasi.entries.forEach { rasiOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = PanchangaAttr.rasiName(rasiOption, selectedLang),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    selectedRasi = rasiOption
                                    isRasiDropdownExpanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                modifier = Modifier.requiredHeight(36.dp)
                            )
                        }
                    }
                }

                // 4. Submit Button
                Button(
                    enabled = isValid,
                    onClick = {
                        if (isValid) {
                            onSetupSubmitted(nameInput.trim(), selectedRasi, selectedLang)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0F172A),
                        disabledContainerColor = Color.White.copy(alpha = 0.3f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = LocaleManager.getString("submit_btn", selectedLang),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
} // function usersetup dialog ends

@Composable
fun PointerToMenuAt75Percent(
    textColor: Color,
    message: String = "Tap the menu icon right here to create your first card!"
) {
    // 1. Infinite bounce transition
    val infiniteTransition = rememberInfiniteTransition(label = "pointer_bounce")

    // 2. Animate bounce offset moving down & slightly right toward (RHS, 750px)
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Message sits directly above the arrow
        Text(
            text = message,
            style = MaterialTheme.typography.titleSmall,
            color = Color.Yellow,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(bottom = 8.dp, start = 24.dp)
        )

        // Arrow pointing DOWN-RIGHT toward the button at Y=750
        Icon(
            imageVector = Icons.Default.KeyboardDoubleArrowDown, // or custom finger asset
            contentDescription = "Point to menu",
            tint = textColor,
            modifier = Modifier
                .padding(end = 8.dp) // Aligns with RHS menu button
                .size(40.dp)
                .rotate(-20f) // Slight tilt toward the right edge
                .offset(
                    x = (bounceOffset * 0.5f).dp, // Bounces slightly right
                    y = bounceOffset.dp          // Bounces down toward Y=750
                )
        )
    }
}