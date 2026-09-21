package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.theme.*

@Composable
fun AnalyzerScreen() {\n    val context = androidx.compose.ui.platform.LocalContext.current\n    var fileName by remember { mutableStateOf<String?>(null) }\n    var analyzing by remember { mutableStateOf(false) }\n    var result by remember { mutableStateOf<com.whoman.fretbible.analyzer.AnalyzerResult?>(null) }\n    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->\n        if (uri != null) {\n            fileName = uri.lastPathSegment ?: "audio file"\n            analyzing = true\n            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {\n                try {\n                    val decoded = com.whoman.fretbible.analyzer.AudioFileDecoder.decode(context, uri)\n                    val env = com.whoman.fretbible.analyzer.SignalFeatures.rmsEnvelope(decoded.samples)\n                    val bpm = com.whoman.fretbible.analyzer.MusicAnalyzer.estimateBpm(env, decoded.sampleRate, 512)\n                    val chroma = com.whoman.fretbible.analyzer.SignalFeatures.chroma(decoded.samples, decoded.sampleRate)\n                    val key = com.whoman.fretbible.analyzer.MusicAnalyzer.estimateKey(chroma)\n                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { result = com.whoman.fretbible.analyzer.AnalyzerResult(bpm.first,bpm.second,key.first,key.second,emptyList()); analyzing=false }\n                } catch (_: Exception) { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { analyzing=false } }\n            }\n        }\n    }
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
                Text(fileName ?: "Drop an audio file here", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text("Key · BPM · chords · guitar tab", color = TextSecondary)
                OutlinedButton(onClick = { launcher.launch(arrayOf("audio/*")) }, modifier = Modifier.fillMaxWidth()) { Text(if (analyzing) "ANALYZING…" else "UPLOAD AUDIO") }
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
                if (result != null) {\n            Text("RESULTS", color = TextMuted, style = MaterialTheme.typography.labelLarge)\n            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {\n                ResultCard("KEY", result!!.key, "${(result!!.keyConfidence*100).roundToInt()}%", Modifier.weight(1f))\n                ResultCard("BPM", "%.1f".format(result!!.bpm), "${(result!!.bpmConfidence*100).roundToInt()}%", Modifier.weight(1f))\n            }\n        }\n\n        Text("COMING NEXT", color = Lime, style = MaterialTheme.typography.labelLarge)
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
\n@Composable private fun ResultCard(label:String,value:String,confidence:String,modifier:Modifier){ Surface(modifier=modifier,shape=RoundedCornerShape(18.dp),color=Surface){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(label,color=TextMuted,style=MaterialTheme.typography.labelSmall);Text(value,color=TextPrimary,style=MaterialTheme.typography.titleLarge);Text(confidence,color=Lime,style=MaterialTheme.typography.labelSmall)}}}\n