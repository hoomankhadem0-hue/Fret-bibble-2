package com.whoman.fretbible.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
    var entered by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "splash")
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val scale by animateFloatAsState(
        targetValue = if (entered) 1f else .78f,
        animationSpec = spring(dampingRatio = .72f, stiffness = 220f),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        delay(120)
        entered = true
        delay(1550)
        onFinished()
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground()

        Box(
            Modifier.align(Alignment.Center).size(210.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.size((150 * pulse).dp)
                    .graphicsLayer { alpha = .10f }
                    .background(Lime, CircleShape)
            )
            Box(
                Modifier.size(105.dp)
                    .graphicsLayer { alpha = .08f; scaleX = pulse; scaleY = pulse }
                    .background(Lime, CircleShape)
            )

            Column(
                Modifier.graphicsLayer {
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "FRET BIBLE",
                    color = TextPrimary,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.5.sp
                )
                Spacer(Modifier.height(8.dp))
                Text("made by Hooman", color = TextMuted, fontSize = 13.sp)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(3) { i ->
                        val dot by transition.animateFloat(
                            initialValue = .35f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                tween(700, delayMillis = i * 160),
                                RepeatMode.Reverse
                            ),
                            label = "dot$i"
                        )
                        Box(
                            Modifier.size(5.dp)
                                .graphicsLayer { alpha = dot }
                                .background(Lime, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
