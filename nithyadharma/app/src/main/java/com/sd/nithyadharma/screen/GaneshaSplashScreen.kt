package com.sd.nithyadharma.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.sd.nithyadharma.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun GaneshaSplashScreen(
    onSplashComplete: () -> Unit
) {
    // Animation values
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 3500, easing = LinearEasing),
        label = "imageFade"
    )

    val diyaScale by animateFloatAsState(
        targetValue = if (isVisible) 1.3f else 0.4f,
        animationSpec = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
        label = "diyaGrow"
    )

    val diyaAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = tween(durationMillis = 1800, delayMillis = 400),
        label = "diyaBrighten"
    )

    // Trigger animation on enter
    LaunchedEffect(Unit) {
        delay(600)           // small pause before starting
        isVisible = true
        delay(2200)          // total show time
        onSplashComplete()   // go to main screen
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Background image – starts invisible, fades in
        Image(
            painter = painterResource(id = R.drawable.bgvorig),
            contentDescription = "Ganesha",
            modifier = Modifier
                .align(Alignment.Center)      // Center the image
                .fillMaxWidth(0.7f)           // Scale to 70% of screen width
                .alpha(alpha)                 // Use your existing fade-in animation
                .graphicsLayer {
                    // This creates the "grunge merge" by fading the edges
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    // Radial gradient mask to soften edges into the black background
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            center = center,
                            radius = size.width * 0.5f
                        ),
                        blendMode = BlendMode.DstIn
                    )
                },
            contentScale = ContentScale.Fit // Ensures the whole image fits in the 70% area
        )
        // Diya light effect (gradient glow from bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(diyaAlpha * 0.7f) // overall glow strength
                .background(
                    Brush.verticalGradient(
                        0.75f to Color.Transparent,
                        0.92f to Color(0xFFFFA500).copy(alpha = 0.35f), // orange-yellow glow
                        0.98f to Color(0xFFFFF176).copy(alpha = 0.55f), // bright flame center
                        1f to Color(0xFFFFF176).copy(alpha = 0.25f)
                    )
                )
        )

        // Small animated diya flame / lamp icon at bottom center
        Icon(
            imageVector = Icons.Default.Lightbulb, // or use your own diya drawable
            contentDescription = "Diya",
            tint = Color(0xFFFFF176), // bright yellow
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(64.dp)
                .scale(diyaScale)
                .alpha(diyaAlpha)
        )
    }
}