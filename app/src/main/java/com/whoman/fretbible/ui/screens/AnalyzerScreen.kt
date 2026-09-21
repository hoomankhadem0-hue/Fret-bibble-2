package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.theme.*

@Composable
fun AnalyzerScreen() {
    Column(
        Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("ANALYZER", color = Lime, style = MaterialTheme.typography.labelLarge)
        Text("Hear the song.", color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
        Text("Upload a track and turn sound into useful guitar information.", color = TextSecondary)

        Card(colors = CardDefaults.cardColors(containerColor = ElevatedSurface), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("TRACK", color = TextMuted, style = MaterialTheme.typography.labelLarge)
                Text("Drop an audio file here", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text("Key · BPM · chords · guitar tab", color = TextSecondary)
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("UPLOAD AUDIO") }
                Text("Analyzer demo · local processing pipeline", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }

        Text("ANALYSIS ROADMAP", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        AnalyzerStep("01", "KEY & SCALE", "Detect the tonal center and likely scale.")
        AnalyzerStep("02", "TEMPO", "Estimate BPM and beat grid.")
        AnalyzerStep("03", "CHORDS", "Track chord changes across the song.")
        AnalyzerStep("04", "GUITAR TAB", "Experimental guitar transcription from isolated or clear guitar parts.")

        Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("COMING NEXT", color = Lime, style = MaterialTheme.typography.labelLarge)
                Text("Upload → Analyze → Review → Practice", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text("Every result will show a confidence level so uncertain detections are clearly marked.", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun AnalyzerStep(number: String, title: String, description: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Surface, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(number, color = Lime, style = MaterialTheme.typography.titleLarge)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(description, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
