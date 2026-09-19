package com.whoman.fretbible.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whoman.fretbible.ui.components.AnimatedBackground
import com.whoman.fretbible.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (visible) 1f else 0.92f, spring(stiffness = Spring.StiffnessLow), label = "logoScale")
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(550), label = "logoAlpha")
    LaunchedEffect(Unit) { visible = true; delay(1450); onFinished() }
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground()
        Column(
            Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha; scaleX = scale; scaleY = scale },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("FRET BIBLE", color = TextPrimary, fontSize = 40.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text("made by Who?man", color = TextMuted, fontSize = 13.sp)
            Spacer(Modifier.height(14.dp))
            Text("●", color = Lime, fontSize = 11.sp)
        }
    }
}
