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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.audio.AudioSettings
import com.whoman.fretbible.practice.PracticeStatsStore
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*

private const val CREATOR_IMAGE_B64 = "${B64_PLACEHOLDER}"

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