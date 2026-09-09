package com.sd.nithyadharma.cards

import LocaleManager
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.sd.nithyadharma.R
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.Constants
import com.sd.nithyadharma.util.LocalAppLanguage
import com.sd.nithyadharma.util.PreferencesManager

// --- 2. CARD CONTENT COMPOSABLE ---
@Composable
fun LightALampCardContent(
    textColor: Color
) {
    val currentLang = LocalAppLanguage.current

    val imageShape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- Banner Image ---
        Image(
            painter = painterResource(id = R.drawable.oldtemple),
            contentDescription = "Ancient temple with oil lamp",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 1f)
                .clip(imageShape)
                .animateContentSize()
        )

        // --- Initial Message ---
        Text(
            text = LocaleManager.getString("ll_msg1", currentLang),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = textColor ,
                lineHeight = 1.7.em // 👈 1.4x the font size (140% line height)
            )
        )

        // --- QR Code Section ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.nd_trust_qrcode),
                contentDescription = "UPI QR Code for NithyaDharma Charitable Trust",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f / 1f)
                    .clip(imageShape)
                    .background(Color.White)
                    .padding(8.dp)
            )
        }

        // --- Contribution / Bank Remittance / Usage Text ---
        Text(
            text = buildAnnotatedString {
                append(
                    LocaleManager.getString(
                        "ll_contrib",
                        currentLang,
                        Constants.NITHYADHARMA_BUSINESS_NUMBER
                    )
                )
                append("\n\n")
                append(
                    LocaleManager.getString(
                        "ll_bankremit",
                        currentLang,
                        "Nithya Darma Charitable Trust\nAccount number: 510909010346099\nIFSC code: CIUB0000152"
                    )
                )
                append("\n\n")
                append(LocaleManager.getString("ll_usage", currentLang))
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = textColor,
                lineHeight = 1.7.em // 👈 1.4x the font size (140% line height)

            )
        )

        // --- Bottom Text Note (Replaces BottomAppBar from screen) ---
        Text(
            text = LocaleManager.getString("ll_bottom", currentLang),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = textColor,
                lineHeight = 1.7.em // 👈 1.4x the font size (140% line height)
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}