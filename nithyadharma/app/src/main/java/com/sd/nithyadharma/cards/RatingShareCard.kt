package com.sd.nithyadharma.cards

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sd.nithyadharma.util.LocalAppLanguage

private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.sd.nithyadharma"

private fun shareApp(
    context: Context
) {
    val shareIntent = Intent(
        Intent.ACTION_SEND
    ).apply {
        type = "text/plain"

        putExtra(
            Intent.EXTRA_TEXT,
            "Discover Nithya Dharma Mobile App for Dharmic living:\n\n$PLAY_STORE_URL"
        )
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            "Share Nithya Dharma"
        )
    )
}

@Composable
fun RatingShareCardContent(
    textColor: Color,
    onRatingSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val currentLang = LocalAppLanguage.current

    var selectedRating by remember {
        mutableIntStateOf(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Title Header
        Text(
            text = LocaleManager.getString(
                "str_rate_share",
                currentLang
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 0.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = textColor.copy(alpha = 0.2f)
        )

        // Rating and Share Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // RATE SECTION
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = LocaleManager.getString(
                        "str_rate",
                        currentLang
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (rating in 1..5) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    selectedRating = rating
                                    onRatingSelected(rating)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "$rating stars",
                                tint = if (rating <= selectedRating) {
                                    Color.Yellow
                                } else {
                                    Color.Gray
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // SHARE SECTION
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .clickable {
                        shareApp(context)
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Nithya Dharma",
                    tint = textColor
                )

                Text(
                    text = LocaleManager.getString(
                        "str_share",
                        currentLang
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}