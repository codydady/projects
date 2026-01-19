package com.sd.nithyadharma.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.sd.nithyadharma.R
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.Constants
import com.sd.nithyadharma.util.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightALampScreen(
    preferencesManager : PreferencesManager,
    onBackClick: () -> Unit) {
    val scrollState = rememberScrollState()
    val imageShape = RoundedCornerShape(16.dp) // Define rounded corners

    val currentLang by preferencesManager.getSelectedLanguage()
        .collectAsState(initial = NDLanguage.EN)

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                title = { Text(LocaleManager.getString("ll_title", currentLang)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    LocaleManager.getString("ll_bottom", currentLang),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp), // Apply general padding to the column
            verticalArrangement = Arrangement.spacedBy(12.dp) // Consistent vertical spacing
        ) {
            // --- First Image (Temple) - Cleaned up and maintaining 2:1 ratio ---
            Image(
                painter = painterResource(id = R.drawable.oldtemple),
                contentDescription = "Ancient temple with oil lamp",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 1f) // Wider ratio for the banner image
                    .clip(imageShape) // Apply rounded corners
                    .animateContentSize()
            )

            Text( LocaleManager.getString("ll_msg1", currentLang) )

            // --- QR Code Image - Refactored to be Centered, Square, and Boxed ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nd_trust_qrcode),
                    contentDescription = "UPI QR Code for NithyaDharma Charitable Trust", // Corrected description
                    contentScale = ContentScale.Fit, // Ensures the QR code is fully visible and readable
                    modifier = Modifier
                        .fillMaxWidth(0.9f) // Confines the size to 60% of the screen width
                        .aspectRatio(1f / 1f) // Forces a square shape for the QR code
                        .clip(imageShape) // Applies the nice rounded corners
                        .background(Color.White) // Gives a clean white background for contrast
                )
            }

            Text(
                text = buildAnnotatedString {
                    append(LocaleManager.getString("ll_contrib", currentLang,
                        "7695803124@upi", Constants.NITHYADHARMA_BUSINESS_NUMBER) )
                    append("\n")
                    append(LocaleManager.getString("ll_bankremit", currentLang,"Nithya Darma Charitable Trust\n" +
                            "Account number: 510909010346099\nIFSC code: CIUB0000152"))
                    append("\n\n")
                    append(LocaleManager.getString("ll_usage", currentLang))
                }
            )
        }
    }
}