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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.core.model.DetectedNote
import com.whoman.fretbible.ui.theme.*
import kotlin.math.abs

private const val IN_TUNE_CENTS = 5.0

@Composable
fun PitchMeter(
    detected: DetectedNote?,
    targetMidi: Int? = null,
    modifier: Modifier = Modifier
) {
    val deviation = if (detected != null && targetMidi != null) {
        (detected.midi - targetMidi) * 100.0 + detected.cents
    } else detected?.cents
    val target = ((deviation ?: 0.0).coerceIn(-50.0, 50.0) / 50.0).toFloat()
    val animated = animateFloatAsState(target, label = "pitchMeter").value
    val inTune = deviation != null && abs(deviation) <= IN_TUNE_CENTS
    val markerColor = if (inTune) Lime else if (deviation == null) TextMuted else Error
    val centsLabel = deviation?.let { (if (it >= 0) "+" else "") + it.toInt() + "¢" } ?: "—"

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("TUNING", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                Text(
                    when {
                        deviation == null -> "PLAY A NOTE"
                        inTune -> "IN TUNE"
                        deviation < 0 -> "TUNE UP"
                        else -> "TUNE DOWN"
                    },
                    color = markerColor,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = markerColor.copy(alpha = if (deviation == null) .08f else .14f),
                border = androidx.compose.foundation.BorderStroke(1.dp, markerColor.copy(alpha = .35f))
            ) {
                Text(
                    centsLabel,
                    color = markerColor,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                )
            }
        }

        Surface(shape = RoundedCornerShape(14.dp), color = Background) {
            Canvas(Modifier.fillMaxWidth().height(54.dp)) {
                val left = 12f
                val right = size.width - 12f
                val center = size.width / 2f
                val y = size.height / 2f
                val trackHeight = 7f
                val safeWidth = right - left
                // A calm green center band represents the accepted ±5-cent tuning window.
                drawRoundRect(
                    color = Border,
                    topLeft = Offset(left, y - trackHeight / 2f),
                    size = Size(safeWidth, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight)
                )
                val toleranceWidth = safeWidth * (IN_TUNE_CENTS / 50.0).toFloat()
                drawRoundRect(
                    color = Lime.copy(alpha = .38f),
                    topLeft = Offset(center - toleranceWidth, y - trackHeight / 2f),
                    size = Size(toleranceWidth * 2f, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight)
                )
                for (step in -5..5) {
                    val x = center + safeWidth * step / 10f
                    drawLine(
                        color = if (step == 0) TextPrimary.copy(alpha = .8f) else TextMuted.copy(alpha = .55f),
                        start = Offset(x, y - if (step == 0) 15f else 8f),
                        end = Offset(x, y + if (step == 0) 15f else 8f),
                        strokeWidth = if (step == 0) 2.5f else 1.2f,
                        cap = StrokeCap.Round
                    )
                }
                val markerX = center + animated * (safeWidth / 2f)
                drawCircle(markerColor.copy(alpha = .2f), 15f, Offset(markerX, y))
                drawCircle(markerColor, 8f, Offset(markerX, y))
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                detected?.let { it.note.display + it.octave } ?: "—",
                color = if (detected == null) TextMuted else TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                detected?.let { "%.1f Hz".format(it.frequencyHz) } ?: "Waiting for input",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text("Green = within ±5¢ · Red = tune the string", color = TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}
