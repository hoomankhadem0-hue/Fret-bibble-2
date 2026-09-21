package com.whoman.fretbible.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
        .94f, 1.06f,
        infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        entered = true
        delay(1350)
        onFinished()
    }

    Box(Modifier.fillMaxSize().background(Background)) {
        AnimatedBackground()

        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(500)) + scaleIn(tween(550), initialScale = .86f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(96.dp).scale(pulse),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.size(96.dp).alpha(.08f).background(Lime, CircleShape))
                        Box(Modifier.size(52.dp).background(LimeSoft, CircleShape))
                        Text("F", color = Lime, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "FRET BIBLE",
                        color = TextPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.6.sp
                    )
                    Spacer(Modifier.height(7.dp))
                    Text("made by Hooman", color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(22.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(3) { index ->
                            Box(
                                Modifier
                                    .size(if (index == 0) 6.dp else 4.dp)
                                    .background(
                                        Lime.copy(alpha = when(index) { 0 -> 1f; 1 -> .55f; else -> .25f }),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
