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
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("PROFILE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        Text("Who?man", style = MaterialTheme.typography.headlineLarge)
        Text("Electric guitar · Standard tuning", color = TextSecondary)
        HorizontalDivider(color = Border)

        Text("AUDIO", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        Text("Microphone sensitivity", style = MaterialTheme.typography.titleLarge)
        Text("Lower the threshold if the app only hears very loud guitar notes.", color = TextSecondary)
        Slider(
            value = sensitivity,
            onValueChange = {
                sensitivity = it
                AudioSettings.sensitivity = it
            },
            valueRange = 0.0005f..0.012f,
            steps = 22
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("MORE SENSITIVE", color = Lime, style = MaterialTheme.typography.labelSmall)
            Text("%.4f".format(sensitivity), color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text("LESS SENSITIVE", color = TextMuted, style = MaterialTheme.typography.labelSmall)
        }
        Text("Input mode: automatic (UNPROCESSED → MIC fallback)", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        Text("Tip: start with the slider near the middle. If quiet notes are missed, move toward MORE SENSITIVE.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        HorizontalDivider(color = Border)
        Text("FRETBOARD", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(onClick = onFretboard, modifier = Modifier.fillMaxWidth()) {
            Text("EXPLORE ALL FRETS & NOTES")
        }

        HorizontalDivider(color = Border)
        Text("PRACTICE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        SettingsRow("Practice tolerance", "±35 cents")
        SettingsRow("Tuning", "Standard E A D G B E")
        HorizontalDivider(color = Border)
        Text("FRET BIBLE", color = Lime, style = MaterialTheme.typography.labelLarge)
        Text("made by Hooman", color = TextSecondary)
    }
}

@Composable
private fun SettingsRow(title: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title)
        Text(value, color = TextSecondary)
    }
}
