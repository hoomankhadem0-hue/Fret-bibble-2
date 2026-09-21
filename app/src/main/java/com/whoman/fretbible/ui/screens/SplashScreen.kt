package com.whoman.fretbible.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whoman.fretbible.R
import com.whoman.fretbible.ui.components.AnimatedBackground
import com.whoman.fretbible.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var entered by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "splash")
    val pulse by transition.animateFloat(
        .97f, 1.03f,
        infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        entered = true
        delay(1450)
        onFinished()
    }

    Box(Modifier.fillMaxSize().background(Background)) {
        AnimatedBackground()
        AnimatedVisibility(
            visible = entered,
            enter = fadeIn(tween(450)) + scaleIn(tween(550), initialScale = .9f)
        ) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    Modifier.size(148.dp).graphicsLayer { scaleX = pulse; scaleY = pulse },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(40.dp),
                        color = Color(0xFF0A0D0A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Lime.copy(alpha = .16f))
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_fret_bible_mark),
                            contentDescription = "Fret Bible",
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("FRET BIBLE", color = TextPrimary, fontSize = 35.sp, fontWeight = FontWeight.Black, letterSpacing = 2.2.sp)
                Spacer(Modifier.height(6.dp))
                Text("made for the neck", color = TextMuted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
        }
    }
}
