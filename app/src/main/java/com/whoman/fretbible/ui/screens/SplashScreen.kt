package com.whoman.fretbible.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
    val infinite = rememberInfiniteTransition(label = "splash")
    val pulse by infinite.animateFloat(
        initialValue = 0.82f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.22f, targetValue = 0.58f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(500), label = "alpha")
    val scale by animateFloatAsState(if (visible) 1f else .88f, spring(stiffness = Spring.StiffnessMediumLow), label = "scale")

    LaunchedEffect(Unit) {
        visible = true
        delay(1850)
        onFinished()
    }

    Box(Modifier.fillMaxSize().background(Background)) {
        AnimatedBackground()
        Column(
            Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha; scaleX = scale; scaleY = scale },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(86.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(62.dp).scale(pulse).background(Lime.copy(alpha = glowAlpha * .20f), CircleShape)
                )
                Box(
                    Modifier.size(12.dp).background(Lime, CircleShape)
                )
            }
            Spacer(Modifier.height(18.dp))
            Text("FRET BIBLE", color = TextPrimary, fontSize = 40.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Spacer(Modifier.height(7.dp))
            Text("PLAY IT. KNOW IT.", color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(10.dp))
            Text("made by Who?man", color = TextMuted, fontSize = 13.sp)
        }
    }
}
