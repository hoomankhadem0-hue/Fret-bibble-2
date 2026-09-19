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
    modifier: Modifier = Modifier
) {
    val p = rememberInfiniteTransition(label = "fretPulse")
    val alpha by p.animateFloat(.45f, 1f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "pulse")
    Canvas(modifier.fillMaxWidth().height(180.dp)) {
        val left = 18f
        val right = size.width - 18f
        val top = 16f
        val bottom = size.height - 16f

        for (s in 0..5) {
            val y = top + (bottom - top) * s / 5f
            drawLine(TextMuted.copy(alpha = .8f), Offset(left, y), Offset(right, y), strokeWidth = 2f + s * .35f, cap = StrokeCap.Round)
        }

        for (fret in 0..12) {
            val x = left + (right - left) * fret / 12f
            drawLine(Border, Offset(x, top), Offset(x, bottom), strokeWidth = if (fret == 0) 8f else 2f)
        }

        listOf(3, 5, 7, 9).forEach { fret ->
            val x = left + (right - left) * (fret - .5f) / 12f
            drawCircle(TextMuted.copy(alpha = .45f), 4f, Offset(x, (top + bottom) / 2f))
        }

        val x12 = left + (right - left) * 11.5f / 12f
        drawCircle(TextMuted.copy(alpha = .45f), 4f, Offset(x12, (top + bottom) / 2f - 24f))
        drawCircle(TextMuted.copy(alpha = .45f), 4f, Offset(x12, (top + bottom) / 2f + 24f))

        if (highlightedString != null && highlightedFret != null) {
            val string = highlightedString.coerceIn(1, 6)
            val fret = highlightedFret.coerceIn(0, 12)
            val y = top + (bottom - top) * (string - 1) / 5f
            val x = if (fret == 0) left else left + (right - left) * (fret - .5f) / 12f

            drawCircle(Lime.copy(alpha = alpha), 17f, Offset(x, y))
            drawCircle(Lime, 8f, Offset(x, y))
        }
    }
}
