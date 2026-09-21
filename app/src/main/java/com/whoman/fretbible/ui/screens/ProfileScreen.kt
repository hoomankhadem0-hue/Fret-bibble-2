package com.whoman.fretbible.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.audio.AudioSettings
import com.whoman.fretbible.practice.PracticeStatsStore
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*

private const val CREATOR_IMAGE_B64 = "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"

@Composable
fun ProfileScreen(userName: String, onUserNameChanged: (String) -> Unit) {
    val context = LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var threshold by remember { mutableFloatStateOf(AudioSettings.sensitivity) }
    var draftName by remember(userName) { mutableStateOf(userName) }
    var showRename by remember { mutableStateOf(false) }
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
        Triple("SHARP MEMORY", "Reach 80% accuracy.", stats.attempts > 0 && stats.accuracy >= 80)
    )

    val pulse = rememberInfiniteTransition(label = "profilePulse")
    val ringScale by pulse.animateFloat(
        .96f, 1.04f,
        infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ringScale"
    )
    val cardAlpha by pulse.animateFloat(
        .96f, 1f,
        infiniteRepeatable(tween(2100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "cardAlpha"
    )

    val creatorBitmap = remember {
        runCatching {
            val bytes = Base64.decode(CREATOR_IMAGE_B64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader(
                kicker = "PROFILE",
                title = "Your space.",
                subtitle = "Achievements, settings and creator links."
            )

            SectionLabel("YOUR PROFILE")
            SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(62.dp), contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(62.dp).scale(ringScale),
                            shape = CircleShape,
                            color = Lime.copy(alpha = .05f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Lime.copy(alpha = .35f))
                        ) {}
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = LimeSoft
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(userName.firstOrNull()?.uppercase() ?: "?", color = Lime, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(userName, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text(stats.sessions.toString() + " sessions · " + stats.correct.toString() + " correct notes", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { draftName = userName; showRename = true }) {
                        Text("EDIT", color = Lime)
                    }
                }
            }

            SectionLabel("ACHIEVEMENTS")
            SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                achievements.forEachIndexed { index, item ->
                    val unlocked = item.third
                    Row(Modifier.fillMaxWidth().padding(vertical = if (index == 0) 0.dp else 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = if (unlocked) LimeSoft else Background) {
                            Text(if (unlocked) "✓" else "·", color = if (unlocked) Lime else TextMuted, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(item.first, color = if (unlocked) TextPrimary else TextSecondary, style = MaterialTheme.typography.titleSmall)
                            Text(item.second, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(if (unlocked) "UNLOCKED" else "LOCKED", color = if (unlocked) Lime else TextMuted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            SectionLabel("SETTINGS")
            SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                Text("MICROPHONE", color = Lime, style = MaterialTheme.typography.labelMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text("Sensitivity", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
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
                    Text("LESS", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    Text("MORE", color = Lime, style = MaterialTheme.typography.labelSmall)
                }
                SecondaryAction("RESET", onClick = {
                    threshold = 0.003f
                    AudioSettings.setSensitivity(context, threshold)
                }, modifier = Modifier.fillMaxWidth())
            }

            SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text("PRACTICE", color = Lime, style = MaterialTheme.typography.labelMedium)
                SettingRow("Pitch", "Note + octave")
                SettingRow("Correctness", "Cents do not block a correct note")
                SettingRow("Tuning", "Standard E A D G B E")
            }

            SectionLabel("ABOUT THE CREATOR")
            SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                if (creatorBitmap != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = androidx.compose.ui.graphics.Color(0xFFF4F1E9)
                    ) {
                        Image(
                            bitmap = creatorBitmap.asImageBitmap(),
                            contentDescription = "Creator sketch",
                            modifier = Modifier.fillMaxWidth().height(210.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            alpha = cardAlpha
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(210.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = LimeSoft
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Creator artwork", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Hooman", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                ContactRow("Instagram", "@Its__whoman") {
                    uriHandler.openUri("https://instagram.com/Its__whoman")
                }
                ContactRow("Email", "hooman.khadem0@gmail.com") {
                    uriHandler.openUri("mailto:hooman.khadem0@gmail.com")
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (showRename) {
            AlertDialog(
                onDismissRequest = { showRename = false },
                containerColor = ElevatedSurface,
                title = { Text("Your name", color = TextPrimary) },
                text = {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it.take(24) },
                        singleLine = true,
                        label = { Text("Name") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val next = draftName.trim()
                        if (next.isNotEmpty()) {
                            onUserNameChanged(next)
                            showRename = false
                        }
                    }) { Text("SAVE", color = Lime) }
                },
                dismissButton = {
                    TextButton(onClick = { showRename = false }) { Text("CANCEL", color = TextMuted) }
                }
            )
        }
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
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = TextMuted, style = MaterialTheme.typography.labelSmall)
                Text(value, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            }
            Text("OPEN ↗", color = Lime, style = MaterialTheme.typography.labelSmall)
        }
    }
}