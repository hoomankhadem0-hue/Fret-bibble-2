package com.whoman.fretbible.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.whoman.fretbible.audio.AudioEngine
import com.whoman.fretbible.practice.*
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PracticeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audio = remember { AudioEngine() }
    val engine = remember { PracticeEngine() }
    val detected by audio.detected.collectAsState()
    val targets = remember { engine.targets() }

    var index by remember { mutableIntStateOf(0) }
    var wrong by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var points by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf(AttemptState.LISTENING) }
    var running by remember { mutableStateOf(false) }
    var denied by remember { mutableStateOf(false) }
    var elapsed by remember { mutableIntStateOf(0) }
    var stableFrames by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var lastMidi by remember { mutableIntStateOf(-999) }
    var locked by remember { mutableStateOf(false) }

    fun resetStability() {
        stableFrames = 0
        lastMidi = -999
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        denied = !granted
        if (granted) {
            running = true
            scope.launch { audio.start() }
        }
    }

    DisposableEffect(Unit) { onDispose { audio.stop() } }

    LaunchedEffect(running) {
        if (running) while (running) {
            delay(1000)
            elapsed++
        }
    }

    LaunchedEffect(detected, index, running) {
        if (!running || detected == null || index >= targets.size || locked) return@LaunchedEffect

        val d = detected ?: return@LaunchedEffect
        if (d.confidence < 0.18) {
            feedback = AttemptState.LISTENING
            return@LaunchedEffect
        }

        val same = d.midi == lastMidi
        stableFrames = if (same) stableFrames + 1 else 1
        lastMidi = d.midi

        if (stableFrames < 2) {
            feedback = AttemptState.LISTENING
            return@LaunchedEffect
        }

        val state = engine.evaluate(targets[index], d)
        feedback = state

        if (state == AttemptState.CORRECT) {
            correct++
            streak++
            points += 100 + ((streak - 1).coerceAtLeast(0) * 25)
            engine.record(targets[index], true)
            locked = true

            delay(550)

            if (index < targets.lastIndex) {
                index++
                feedback = AttemptState.LISTENING
                resetStability()
                locked = false
            } else {
                running = false
                audio.stop()
            }
        } else {
            wrong++
            streak = 0
            engine.record(targets[index], false)
            locked = true

            delay(350)

            feedback = AttemptState.LISTENING
            resetStability()
            locked = false
        }
    }

    val current = targets[index.coerceIn(0, targets.lastIndex)]
    val accuracy = if (correct + wrong == 0) 0 else correct * 100 / (correct + wrong)

    val statusText = when (feedback) {
        AttemptState.CORRECT -> "CORRECT · +POINTS"
        AttemptState.TOO_HIGH -> "TOO HIGH · PLAY LOWER"
        AttemptState.TOO_LOW -> "TOO LOW · PLAY HIGHER"
        AttemptState.WRONG_NOTE -> "WRONG NOTE · TRY AGAIN"
        AttemptState.LISTENING -> "LISTENING FOR YOUR NOTE"
    }

    val statusColor = when (feedback) {
        AttemptState.CORRECT -> Lime
        AttemptState.TOO_HIGH, AttemptState.TOO_LOW -> Warning
        AttemptState.WRONG_NOTE -> Error
        AttemptState.LISTENING -> TextSecondary
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("PRACTICE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
                Text("${(index + 1).coerceAtMost(targets.size)} of ${targets.size}", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            }
            SessionStat("POINTS", points.toString())
            Spacer(Modifier.width(8.dp))
            SessionStat("STREAK", streak.toString())
        }

        LinearProgressIndicator(
            progress = { (index.toFloat() / targets.size).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(5.dp),
            color = Lime,
            trackColor = Border
        )

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PLAY THIS NOTE", color = TextMuted, style = MaterialTheme.typography.labelLarge)

                AnimatedContent(
                    targetState = current,
                    transitionSpec = {
                        (fadeIn(tween(160)) + slideInVertically(tween(180)) { it / 3 })
                            .togetherWith(fadeOut(tween(100)))
                    },
                    label = "target"
                ) { target ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                        Text(
                            target.note.display + target.octave,
                            color = Lime,
                            style = MaterialTheme.typography.displayMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text("STRING ${target.stringNumber}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            Text(if (target.fret == 0) "OPEN" else "FRET ${target.fret}", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                Text(current.instruction, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                Text("The note name matters. Cents do not.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("FRETBOARD", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    Text(if (current.fret == 0) "OPEN STRING" else "FRET ${current.fret}", color = Lime, style = MaterialTheme.typography.labelSmall)
                }
                Fretboard(
                    highlightedString = current.stringNumber,
                    highlightedFret = current.fret,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (running) "MIC ACTIVE" else "READY", color = if (running) Lime else TextMuted, style = MaterialTheme.typography.labelLarge)
                        Text(statusText, color = statusColor, style = MaterialTheme.typography.titleMedium)
                    }
                    if (detected != null) {
                        Text("${detected!!.note.display}${detected!!.octave}", color = TextPrimary, style = MaterialTheme.typography.headlineSmall)
                    }
                }

                PitchMeter(detected)

                if (detected != null) {
                    Text(
                        "${detected!!.frequencyHz.toInt()} Hz  ·  ${if (detected!!.cents >= 0) "+" else ""}${detected!!.cents.toInt()}¢",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text("Play one clean note. Mute the other strings.", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                }

                if (feedback == AttemptState.CORRECT) {
                    Text(
                        "+${100 + ((streak - 1).coerceAtLeast(0) * 25)} POINTS",
                        color = Lime,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        if (denied) {
            Text("Microphone permission is required for live note detection.", color = Error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    running = true
                    scope.launch { audio.start() }
                } else {
                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            enabled = !locked,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (running) "LISTENING…" else "START PRACTICE")
        }

        if (!running && index >= targets.lastIndex) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("SESSION COMPLETE", color = Lime, style = MaterialTheme.typography.labelLarge)
                    Text("${points} POINTS", style = MaterialTheme.typography.headlineMedium)
                    Text("${accuracy}% accuracy · ${correct} correct · ${elapsed} seconds", color = TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SessionStat(label: String, value: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = Surface) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = Lime, style = MaterialTheme.typography.titleMedium)
        }
    }
}
