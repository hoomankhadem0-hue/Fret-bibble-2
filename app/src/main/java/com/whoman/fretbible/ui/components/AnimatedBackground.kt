package com.whoman.fretbible.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.whoman.fretbible.ui.theme.Background
import com.whoman.fretbible.ui.theme.Border
import com.whoman.fretbible.ui.theme.Lime
import kotlin.math.sin

@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "background")
    val phase by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "phase"
    )

    Canvas(modifier.fillMaxSize()) {
        drawRect(Background)
        val x = size.width * (.18f + .42f * phase)
        val y = size.height * (.10f + .08f * sin(phase * Math.PI * 2).toFloat())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Lime.copy(alpha = .045f), Color.Transparent),
                center = Offset(x, y),
                radius = size.minDimension * .48f
            ),
            radius = size.minDimension * .48f,
            center = Offset(x, y)
        )
        drawLine(
            Border.copy(alpha = .18f),
            Offset(size.width * .06f, size.height * .74f),
            Offset(size.width * .94f, size.height * .74f),
            1f
        )
    }
}
