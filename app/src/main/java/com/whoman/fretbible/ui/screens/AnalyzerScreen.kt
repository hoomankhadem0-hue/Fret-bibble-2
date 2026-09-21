package com.whoman.fretbible.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.analyzer.*
import com.whoman.fretbible.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun AnalyzerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fileName by remember { mutableStateOf<String?>(null) }
    var analyzing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<AnalyzerResult?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            fileName = uri.lastPathSegment ?: "audio file"
            analyzing = true
            error = null
            result = null
            scope.launch {
                try {
                    val analyzed = withContext(Dispatchers.Default) {
                        val decoded = AudioFileDecoder.decode(context, uri)
                        val env = SignalFeatures.rmsEnvelope(decoded.samples)
                        val bpm = MusicAnalyzer.estimateBpm(env, decoded.sampleRate, 512)
                        val frames = SignalFeatures.chromaFrames(decoded.samples, decoded.sampleRate)
                        val aggregate = DoubleArray(12)
                        for (frame in frames) for (pc in 0 until 12) aggregate[pc] += frame[pc]
                        val max = aggregate.maxOrNull() ?: 0.0
                        if (max > 0) for (pc in 0 until 12) aggregate[pc] /= max
                        val key = MusicAnalyzer.estimateKey(aggregate)
                        val chords = MusicAnalyzer.estimateChords(frames, decoded.sampleRate, 2048, key = key.first)
                        AnalyzerResult(bpm.first, bpm.second, key.first, key.second, chords, MusicAnalyzer.matchProgressions(chords, key.first))
                    }
                    result = analyzed
                } catch (t: Throwable) {
                    error = t.message ?: "Could not analyze this audio file."
                } finally {
                    analyzing = false
                }
            }
        }
    }

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
                Text(fileName ?: "Choose an audio file", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text("Key · BPM · chord timeline", color = TextSecondary)
                Button(onClick = { launcher.launch(arrayOf("audio/*")) }, enabled = !analyzing, modifier = Modifier.fillMaxWidth()) {
                    Text(if (analyzing) "ANALYZING…" else "UPLOAD AUDIO")
                }
                if (error != null) Text(error!!, color = Error, style = MaterialTheme.typography.bodySmall)
                Text("Local processing · first 3 minutes · experimental", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                Text("Harmonic knowledge base · ${ProgressionLibrary.patterns.size} progression patterns", color = Lime.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
            }
        }

        result?.let { analysis ->
            Text("RESULTS", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResultCard("KEY", analysis.key, "${(analysis.keyConfidence * 100).roundToInt()}%", Modifier.weight(1f))
                ResultCard("BPM", "%.1f".format(analysis.bpm), "${(analysis.bpmConfidence * 100).roundToInt()}%", Modifier.weight(1f))
            }
            if (analysis.progressionMatches.isNotEmpty()) {
                Text("DETECTED PROGRESSIONS", color = TextMuted, style = MaterialTheme.typography.labelLarge)
                Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        analysis.progressionMatches.take(5).forEach { match ->
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(match.pattern.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                                    Text("${(match.score * 100).roundToInt()}%", color = Lime, style = MaterialTheme.typography.labelSmall)
                                }
                                Text("${match.pattern.genre} · ${match.pattern.numerals.take(match.length).joinToString("  ")}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                Text("from ${formatTime(analysis.chords.getOrNull(match.startIndex)?.startSeconds ?: 0.0)} · soft harmonic match", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Text("CHORD TIMELINE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (analysis.chords.isEmpty()) {
                        Text("No stable chords detected.", color = TextSecondary)
                    } else {
                        analysis.chords.take(24).forEachIndexed { index, chord ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(chord.symbol, color = if (index == 0) Lime else TextPrimary, style = MaterialTheme.typography.titleMedium)
                                Text(formatTime(chord.startSeconds), color = TextMuted, style = MaterialTheme.typography.bodySmall)
                                Text("${(chord.confidence * 100).roundToInt()}%", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (analysis.chords.size > 24) Text("+ ${analysis.chords.size - 24} more changes", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (analysis.progressionMatches.isNotEmpty()) {\n            Text("DETECTED PROGRESSIONS", color = TextMuted, style = MaterialTheme.typography.labelLarge)\n            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {\n                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {\n                    analysis.progressionMatches.forEach { match ->\n                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {\n                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {\n                                Text(match.pattern.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)\n                                Text("${(match.score * 100).roundToInt()}%", color = Lime, style = MaterialTheme.typography.labelMedium)\n                            }\n                            Text("${match.pattern.genre} · ${match.pattern.numerals.take(match.length).joinToString("  →  ")}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)\n                            Text("Starts at ${formatTime(analysis.chords.getOrNull(match.startIndex)?.startSeconds ?: 0.0)} · ${match.length} chords", color = TextMuted, style = MaterialTheme.typography.bodySmall)\n                        }\n                    }\n                    Text("Pattern matching is probabilistic and based on detected chords + key.", color = TextMuted, style = MaterialTheme.typography.bodySmall)\n                }\n            }\n        }\n        Text("ANALYSIS ROADMAP", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        AnalyzerStep("01", "KEY & SCALE", "Detect the tonal center and likely scale.")
        AnalyzerStep("02", "TEMPO", "Estimate BPM and beat grid.")
        AnalyzerStep("03", "CHORDS", "Track chord changes across the song.")
        AnalyzerStep("04", "GUITAR TAB", "Experimental transcription from isolated or clear guitar parts.")

        Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("COMING NEXT", color = Lime, style = MaterialTheme.typography.labelLarge)
                Text("Upload → Analyze → Review → Practice", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text("Tab extraction remains experimental; uncertain results will be labeled rather than presented as exact transcription.", color = TextSecondary)
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

@Composable
private fun ResultCard(label: String, value: String, confidence: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = Surface) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            Text(confidence, color = Lime, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun formatTime(seconds: Double): String {
    val total = seconds.toInt().coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}
