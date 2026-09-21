package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.audio.AudioSettings
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*

@Composable
fun ProfileScreen(onAnalyzer: () -> Unit) {
    val context = LocalContext.current
    var threshold by remember { mutableFloatStateOf(0.00035f) }

    LaunchedEffect(Unit) {
        AudioSettings.load(context)
        threshold = AudioSettings.sensitivity
    }

    val min = 0.00005f
    val max = 0.006f
    val sliderValue = ((max - threshold) / (max - min)).coerceIn(0f, 1f)
    val sensitivityLabel = when {
        sliderValue > .72f -> "High"
        sliderValue > .42f -> "Balanced"
        else -> "Low"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(
            kicker = "MORE",
            title = "Tools & settings",
            subtitle = "Keep the learning flow focused. Advanced tools live here."
        )

        SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
            Text("SONG ANALYSIS", color = Lime, style = MaterialTheme.typography.labelMedium)
            Text("Hear a track differently.", style = MaterialTheme.typography.headlineSmall)
            Text("Explore key, tempo and harmonic structure.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            PrimaryAction("OPEN ANALYZER", onAnalyzer, Modifier.fillMaxWidth())
        }

        SectionLabel("AUDIO")
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Mic threshold", style = MaterialTheme.typography.titleMedium)
                    Text("How quiet a note can be before detection is ignored.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Text(sensitivityLabel, color = Lime, style = MaterialTheme.typography.labelMedium)
            }

            Slider(
                value = sliderValue,
                onValueChange = {
                    val next = max - it * (max - min)
                    threshold = next
                    AudioSettings.setSensitivity(context, next)
                }
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("MORE SENSITIVE", color = Lime, style = MaterialTheme.typography.labelSmall)
                Text("LESS SENSITIVE", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }

            SecondaryAction(
                "RESET TO DEFAULT",
                onClick = {
                    threshold = 0.00035f
                    AudioSettings.setSensitivity(context, threshold)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        SectionLabel("PRACTICE RULES")
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            SettingRow("Pitch matching", "Note + octave · fixed for consistent scoring")
            SettingRow("Correctness", "Cents never block a correct note · fixed")
            SettingRow("Tuning", "Standard E · A · D · G · B · E · fixed")
        }

        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Text("SESSION CONTROLS", color = TextMuted, style = MaterialTheme.typography.labelMedium)
            Text("Questions, training mode and fret range are chosen at the beginning of each Practice session.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }

        SectionLabel("ABOUT")
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Text("FRET BIBLE", color = Lime, style = MaterialTheme.typography.labelMedium)
            Text("made by Hooman", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text("Music-tech tools for learning the neck by ear and by hand.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(8.dp))
    }
}
