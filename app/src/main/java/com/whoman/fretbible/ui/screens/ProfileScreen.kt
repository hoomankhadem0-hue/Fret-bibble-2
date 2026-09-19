package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.audio.AudioSettings
import com.whoman.fretbible.ui.theme.*

@Composable
fun ProfileScreen(onFretboard: () -> Unit) {
    var sensitivity by remember { mutableFloatStateOf(AudioSettings.sensitivity) }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("PROFILE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        Text("Who?man", style = MaterialTheme.typography.headlineLarge)
        Text("Electric guitar · Standard tuning", color = TextSecondary)
        HorizontalDivider(color = Border)

        Text("AUDIO", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        Text("Microphone sensitivity", style = MaterialTheme.typography.titleLarge)
        Text(
            "Controls how quiet a note can be before the detector ignores it.",
            color = TextSecondary
        )
        Slider(
            value = sensitivity,
            onValueChange = {
                sensitivity = it
                AudioSettings.sensitivity = it
            },
            valueRange = 0.00005f..0.006f,
            steps = 23
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("MORE SENSITIVE", color = Lime, style = MaterialTheme.typography.labelSmall)
            Text("%.5f".format(sensitivity), color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text("LESS SENSITIVE", color = TextMuted, style = MaterialTheme.typography.labelSmall)
        }
        Text(
            "Input: MIC → UNPROCESSED fallback",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            "Start around the middle. If the app still needs a hard strum, move toward MORE SENSITIVE.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )

        HorizontalDivider(color = Border)
        Text("FRETBOARD", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        Text(
            "Complete standard-tuning map: 6 strings × frets 0–24.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedButton(
            onClick = onFretboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("EXPLORE ALL FRETS & NOTES")
        }

        HorizontalDivider(color = Border)
        Text("PRACTICE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        SettingsRow("Pitch matching", "Note + octave")
        SettingsRow("Tolerance", "Cents do not block a correct note")
        SettingsRow("Tuning", "Standard E A D G B E")

        HorizontalDivider(color = Border)
        Text("FRET BIBLE", color = Lime, style = MaterialTheme.typography.labelLarge)
        Text("made by Hooman", color = TextSecondary)
    }
}

@Composable
private fun SettingsRow(title: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Text(value, color = TextSecondary)
    }
}
