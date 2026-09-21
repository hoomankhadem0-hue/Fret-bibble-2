package com.whoman.fretbible.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.core.model.DetectedNote
import com.whoman.fretbible.ui.theme.*

@Composable
fun PitchMeter(detected: DetectedNote?, modifier: Modifier = Modifier) {
    val target = ((detected?.cents ?: 0.0).coerceIn(-50.0, 50.0) / 50.0).toFloat()
    val animated = animateFloatAsState(target, label = "pitchMeter").value
    val centered = kotlin.math.abs(animated) < .08f

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("FLAT", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text(if (detected == null) "NO SIGNAL" else if (centered) "IN TUNE" else "PITCH", color = if (centered) Lime else TextMuted, style = MaterialTheme.typography.labelSmall)
            Text("SHARP", color = TextMuted, style = MaterialTheme.typography.labelSmall)
        }

        Surface(shape = RoundedCornerShape(12.dp), color = Background) {
            Canvas(Modifier.fillMaxWidth().height(34.dp)) {
                val center = size.width / 2f
                val x = center + animated * (center - 10f)
                drawLine(
                    Border,
                    Offset(10f, size.height / 2f),
                    Offset(size.width - 10f, size.height / 2f),
                    4f
                )
                drawLine(
                    TextMuted,
                    Offset(center, 6f),
                    Offset(center, size.height - 6f),
                    2f
                )
                drawCircle(
                    if (centered) Lime else TextPrimary,
                    8.5f,
                    Offset(x, size.height / 2f)
                )
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                detected?.let { "${it.note.display}${it.octave}" } ?: "—",
                color = if (detected == null) TextMuted else TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            detected?.let {
                Text(
                    "${if (it.cents >= 0) "+" else ""}${it.cents.toInt()}¢",
                    color = if (centered) Lime else TextSecondary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
