package com.whoman.fretbible.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.analyzer.*
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun AnalyzerScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
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
                    result = withContext(Dispatchers.Default) {
                        val d = AudioFileDecoder.decode(context, uri)
                        val env = SignalFeatures.rmsEnvelope(d.samples)
                        val bpm = MusicAnalyzer.estimateBpm(env, d.sampleRate, 512)
                        val frames = SignalFeatures.chromaFrames(d.samples, d.sampleRate)
                        val aggregate = DoubleArray(12)
                        for (frame in frames) for (pc in 0 until 12) aggregate[pc] += frame[pc]
                        val max = aggregate.maxOrNull() ?: 0.0
                        if (max > 0) for (pc in 0 until 12) aggregate[pc] /= max
                        val key = MusicAnalyzer.estimateKey(aggregate)
                        val chords = MusicAnalyzer.estimateChords(frames, d.sampleRate, 2048, key = key.first)
                        AnalyzerResult(
                            bpm.first, bpm.second, key.first, key.second, chords,
                            MusicAnalyzer.matchProgressions(chords, key.first)
                        )
                    }
                } catch (t: Throwable) {
                    error = t.message ?: "Could not analyze this audio file."
                } finally {
                    analyzing = false
                }
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(
            kicker = "ANALYZER",
            title = "Read the song.",
            subtitle = "Turn a track into tempo, key and harmonic landmarks."
        )

        SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("AUDIO INPUT", color = Lime, style = MaterialTheme.typography.labelMedium)
                    Crossfade(fileName ?: "No track selected", label = "fileName") { name ->
                        Text(name, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    }
                    Text("Local processing · up to first 3 minutes", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
                Surface(shape = RoundedCornerShape(14.dp), color = LimeSoft) {
                    Text("WAV", color = Lime, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp))
                }
            }
            PrimaryAction(
                if (analyzing) "ANALYZING…" else if (fileName == null) "CHOOSE AUDIO" else "ANALYZE AGAIN",
                onClick = { launcher.launch(arrayOf("audio/*")) },
                enabled = !analyzing,
                modifier = Modifier.fillMaxWidth()
            )
            error?.let {
                Surface(shape = RoundedCornerShape(12.dp), color = Error.copy(alpha = .08f)) {
                    Text(it, color = Error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                }
            }
        }

        result?.let { a ->
            AnimatedVisibility(visible = true) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionLabel("AT A GLANCE")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AnalyzerMetric("KEY", a.key, confidenceLabel(a.keyConfidence), Modifier.weight(1f))
                        AnalyzerMetric("BPM", "%.1f".format(a.bpm), confidenceLabel(a.bpmConfidence), Modifier.weight(1f))
                    }

                    SectionLabel("HARMONIC FLOW")
                    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        if (a.chords.isEmpty()) {
                            Text("No stable harmonic regions detected.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            a.chords.take(16).forEachIndexed { index, chord ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(9.dp), color = if (index == 0) LimeSoft else Background) {
                                        Text(
                                            chord.symbol,
                                            color = if (index == 0) Lime else TextPrimary,
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.widthIn(min = 54.dp).padding(horizontal = 9.dp, vertical = 8.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(formatTime(chord.startSeconds), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                        LinearProgressIndicator(
                                            progress = { chord.confidence.coerceIn(0.05, 1.0).toFloat() },
                                            modifier = Modifier.fillMaxWidth().height(4.dp),
                                            color = if (chord.confidence >= .65) Lime else Warning,
                                            trackColor = Border
                                        )
                                    }
                                }
                            }
                            if (a.chords.size > 16) Text("+ ${a.chords.size - 16} more chord regions", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (a.progressionMatches.isNotEmpty()) {
                        SectionLabel("PROGRESSION HINTS")
                        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                            a.progressionMatches.take(3).forEach { match ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(match.pattern.name, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            "${match.length} chord regions · ${formatPercent(match.score)} fit",
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text(formatPercent(match.score), color = Lime, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    SectionLabel("ANALYSIS PIPELINE")
                    AnalyzerStep("01", "KEY", "Find the tonal center and confidence.")
                    AnalyzerStep("02", "TEMPO", "Estimate a usable BPM range.")
                    AnalyzerStep("03", "HARMONY", "Follow chord regions across the track.")
                    AnalyzerStep("04", "PROGRESSION", "Compare the observed flow with known patterns.")
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AnalyzerMetric(label: String, value: String, confidence: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = TextPrimary, style = MaterialTheme.typography.headlineSmall)
            Text(confidence, color = Lime, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun AnalyzerStep(number: String, title: String, description: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp), color = LimeSoft) {
                Text(number, color = Lime, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                Text(description, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun confidenceLabel(value: Double): String =
    when {
        value >= .7 -> "HIGH CONFIDENCE"
        value >= .4 -> "MEDIUM CONFIDENCE"
        else -> "LOW CONFIDENCE"
    }

private fun formatPercent(value: Double): String = "${(value * 100).roundToInt()}%"

private fun formatTime(seconds: Double): String {
    val total = seconds.toInt().coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}
