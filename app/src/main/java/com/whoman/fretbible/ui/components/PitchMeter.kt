package com.whoman.fretbible.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.core.model.DetectedNote
import com.whoman.fretbible.ui.theme.*

@Composable
fun PitchMeter(detected: DetectedNote?, modifier: Modifier = Modifier) {
    val target = ((detected?.cents ?: 0.0).coerceIn(-50.0, 50.0) / 50.0).toFloat()
    val animated = animateFloatAsState(target, label = "pitchMeter")
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TOO LOW", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            Text("TOO HIGH", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        }
        Canvas(Modifier.fillMaxWidth().height(22.dp)) {
            val center = size.width / 2f
            val x = center + animated.value * center
            drawLine(Border, androidx.compose.ui.geometry.Offset(0f, size.height / 2), androidx.compose.ui.geometry.Offset(size.width, size.height / 2), 3f)
            drawLine(TextMuted, androidx.compose.ui.geometry.Offset(center, 2f), androidx.compose.ui.geometry.Offset(center, size.height - 2f), 2f)
            drawCircle(Lime, 9f, androidx.compose.ui.geometry.Offset(x, size.height / 2))
        }
        Text(
            detected?.let { it.note.display + it.octave + "  " + (if (it.cents >= 0) "+" else "") + it.cents.toInt() + "¢" } ?: "—",
            color = if (detected == null) TextMuted else TextPrimary,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
