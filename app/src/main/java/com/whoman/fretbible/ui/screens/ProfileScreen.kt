package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.audio.AudioSettings
import com.whoman.fretbible.practice.PracticeStatsStore
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var threshold by remember { mutableFloatStateOf(0.00035f) }
    val stats = remember { PracticeStatsStore.load(context) }

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

    val achievements = listOf(
        Triple("FIRST SESSION", "Complete one practice session.", stats.sessions >= 1),
        Triple("NOTE HUNTER", "Land 100 correct notes.", stats.correct >= 100),
        Triple("10 SESSIONS", "Complete ten practice sessions.", stats.sessions >= 10),
        Triple("SHARP MEMORY", "Reach 80% overall accuracy.", stats.attempts > 0 && stats.accuracy >= 80)
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(
            kicker = "PROFILE",
            title = "Your progress.",
            subtitle = "Achievements, settings and a way to reach the creator."
        )

        SectionLabel("ACHIEVEMENTS")
        SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("BUILD YOUR STREAK", color = Lime, style = MaterialTheme.typography.labelMedium)
                    Text("${stats.sessions} sessions · ${stats.correct} correct notes", style = MaterialTheme.typography.titleMedium)
                    Text("Keep practicing to unlock more milestones.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Surface(shape = CircleShape, color = LimeSoft) {
                    Text("${stats.accuracy}%", color = Lime, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
                }
            }
        }

        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            achievements.forEachIndexed { index, item ->
                val unlocked = item.third
                Row(
                    Modifier.fillMaxWidth().padding(vertical = if (index == 0) 0.dp else 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = if (unlocked) LimeSoft else Background) {
                        Text(
                            if (unlocked) "✓" else "·",
                            color = if (unlocked) Lime else TextMuted,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(item.first, color = if (unlocked) TextPrimary else TextSecondary, style = MaterialTheme.typography.titleSmall)
                        Text(item.second, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        if (unlocked) "UNLOCKED" else "LOCKED",
                        color = if (unlocked) Lime else TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        SectionLabel("SETTINGS")
        SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
            Text("MICROPHONE", color = Lime, style = MaterialTheme.typography.labelMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Sensitivity", style = MaterialTheme.typography.titleMedium)
                    Text("Raise it for quieter playing; lower it to reject more background noise.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
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
                Text("LESS SENSITIVE", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                Text("MORE SENSITIVE", color = Lime, style = MaterialTheme.typography.labelSmall)
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

        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Text("PRACTICE RULES", color = Lime, style = MaterialTheme.typography.labelMedium)
            SettingRow("Pitch matching", "Note + octave · fixed")
            SettingRow("Correctness", "Cents do not block a correct note · fixed")
            SettingRow("Tuning", "Standard E · A · D · G · B · E · fixed")
            Text("Questions, mode and fret range are set at the beginning of each Practice session.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }

        SectionLabel("ABOUT THE CREATOR")
        SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = LimeSoft,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Lime.copy(alpha = .28f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("H", color = Lime, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Made by Hooman", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text("Music-tech tools for learning the neck.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(4.dp))
            ContactRow(
                title = "Instagram",
                value = "@Its__whoman",
                onClick = { uriHandler.openUri("https://instagram.com/Its__whoman") }
            )
            ContactRow(
                title = "Email",
                value = "hooman.khadem0@gmail.com",
                onClick = { uriHandler.openUri("mailto:hooman.khadem0@gmail.com") }
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ContactRow(title: String, value: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = ElevatedSurface.copy(alpha = .55f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = .55f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = TextMuted, style = MaterialTheme.typography.labelSmall)
                Text(value, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            }
            Text("OPEN ↗", color = Lime, style = MaterialTheme.typography.labelSmall)
        }
    }
}
