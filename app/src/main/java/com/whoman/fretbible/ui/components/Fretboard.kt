package com.whoman.fretbible.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.theme.*

@Composable
fun Fretboard(
    highlightedString: Int? = null,
    highlightedFret: Int? = null,
    maxFret: Int = 21,
    modifier: Modifier = Modifier
) {
    val pulse = rememberInfiniteTransition(label = "fretPulse")
    val glowAlpha by pulse.animateFloat(
        .42f, .95f,
        infiniteRepeatable(tween(720, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Canvas(modifier.fillMaxWidth().height(164.dp)) {
        val left = 16f
        val right = size.width - 16f
        val top = 14f
        val bottom = size.height - 14f
        val frets = maxFret.coerceIn(1, 21)

        for (s in 0..5) {
            val y = top + (bottom - top) * s / 5f
            drawLine(
                TextMuted.copy(alpha = .7f),
                Offset(left, y),
                Offset(right, y),
                strokeWidth = 1.8f + s * .32f,
                cap = StrokeCap.Round
            )
        }

        for (fret in 0..frets) {
            val x = left + (right - left) * fret / frets.toFloat()
            drawLine(
                if (fret == 0) TextPrimary.copy(alpha = .82f) else Border.copy(alpha = .86f),
                Offset(x, top),
                Offset(x, bottom),
                strokeWidth = if (fret == 0) 5.5f else 1.7f
            )
        }

        listOf(3, 5, 7, 9, 15, 17, 19).filter { it <= frets }.forEach { fret ->
            val x = left + (right - left) * (fret - .5f) / frets.toFloat()
            drawCircle(TextMuted.copy(alpha = .42f), 3.5f, Offset(x, (top + bottom) / 2f))
        }
        if (12 <= frets) {
            val x = left + (right - left) * 11.5f / frets.toFloat()
            drawCircle(TextMuted.copy(alpha = .38f), 3.5f, Offset(x, (top + bottom) / 2f - 22f))
            drawCircle(TextMuted.copy(alpha = .38f), 3.5f, Offset(x, (top + bottom) / 2f + 22f))
        }

        if (highlightedString != null && highlightedFret != null) {
            val string = highlightedString.coerceIn(1, 6)
            val fret = highlightedFret.coerceIn(0, frets)
            val y = top + (bottom - top) * (string - 1) / 5f
            val x = if (fret == 0) left else left + (right - left) * (fret - .5f) / frets.toFloat()
            drawCircle(Lime.copy(alpha = glowAlpha), 16f, Offset(x, y))
            drawCircle(Lime, 7f, Offset(x, y))
        }
    }
}
