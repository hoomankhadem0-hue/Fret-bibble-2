package com.whoman.fretbible.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.core.content.ContextCompat
import com.whoman.fretbible.audio.AudioEngine
import com.whoman.fretbible.practice.*
import com.whoman.fretbible.ui.components.Fretboard
import com.whoman.fretbible.ui.components.PitchMeter
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

    var targets by remember { mutableStateOf(engine.newSession(24)) }
    var index by remember { mutableIntStateOf(0) }
    var points by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf(AttemptState.LISTENING) }
    var running by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(false) }
    var denied by remember { mutableStateOf(false) }
    var timerEnabled by remember { mutableStateOf(true) }
    var secondsLeft by remember { mutableIntStateOf(10) }
    var stableFrames by remember { mutableIntStateOf(0) }
    var lastMidi by remember { mutableIntStateOf(-999) }
    var sessionStarted by remember { mutableStateOf(false) }
    var sessionStartedAt by remember { mutableLongStateOf(0L) }
    var sessionSaved by remember { mutableStateOf(false) }
    var showTargetPosition by remember { mutableStateOf(true) }

    val current = targets.getOrNull(index)
    val accuracy = if (correct + misses == 0) 0 else correct * 100 / (correct + misses)

    fun resetStability() {
        stableFrames = 0
        lastMidi = -999
    }

    fun saveCurrentSession() {
        if (!sessionStarted || sessionSaved) return
        val elapsed = if (sessionStartedAt > 0L) ((System.currentTimeMillis() - sessionStartedAt) / 1000L) else 0L
        PracticeStatsStore.saveSession(context, points, correct, correct + misses, elapsed)
        sessionSaved = true
    }

    fun finishSession() {
        running = false
        audio.stop()
        saveCurrentSession()
    }

    fun startNewSession() {
        targets = engine.newSession(24)
        index = 0
        points = 0
        correct = 0
        misses = 0
        streak = 0
        secondsLeft = 10
        feedback = AttemptState.LISTENING
        locked = false
        resetStability()
        sessionStarted = true
        sessionSaved = false
        sessionStartedAt = System.currentTimeMillis()
        running = true
        scope.launch { audio.start() }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        denied = !granted
        if (granted) startNewSession()
    }

    DisposableEffect(Unit) {
        onDispose { saveCurrentSession(); audio.stop() }
    }

    // Each target gets its own 10-second clock. Timer OFF means no deadline.
    LaunchedEffect(running, index, timerEnabled, sessionStarted) {
        if (!running || !timerEnabled || current == null || locked) return@LaunchedEffect
        secondsLeft = 10
        while (running && timerEnabled && !locked && secondsLeft > 0) {
            delay(1000)
            if (!locked) secondsLeft--
        }
        if (running && timerEnabled && !locked && secondsLeft == 0) {
            feedback = AttemptState.WRONG_NOTE
            misses++
            streak = 0
            engine.record(current, false)
            locked = true
            delay(300)
            if (index < targets.lastIndex) {
                index++
                secondsLeft = 10
                feedback = AttemptState.LISTENING
                resetStability()
                locked = false
            } else {
                saveCurrentSession()
                finishSession()
            }
        }
    }

    LaunchedEffect(detected, index, running, sessionStarted) {
        if (!running || current == null || detected == null || locked) return@LaunchedEffect

        val d = detected ?: return@LaunchedEffect
        if (d.confidence < 0.18) return@LaunchedEffect

        val same = d.midi == lastMidi
        stableFrames = if (same) stableFrames + 1 else 1
        lastMidi = d.midi
        if (stableFrames < 2) return@LaunchedEffect

        val state = engine.evaluate(current, d)
        feedback = state

        if (state == AttemptState.CORRECT) {
            val speedBonus = if (timerEnabled) secondsLeft * 15 else 0
            val streakBonus = streak * 25
            val earned = 100 + speedBonus + streakBonus

            correct++
            streak++
            points += earned
            engine.record(current, true)
            locked = true

            scope.launch {
                delay(500)
                if (index < targets.lastIndex) {
                    index++
                    secondsLeft = 10
                    feedback = AttemptState.LISTENING
                    resetStability()
                    locked = false
                } else {
                    saveCurrentSession()
                    finishSession()
                }
            }
        } else {
            misses++
            streak = 0
            engine.record(current, false)
            locked = true
            scope.launch {
                delay(300)
                feedback = AttemptState.LISTENING
                resetStability()
                locked = false
            }
        }
    }

    if (current == null) return

    val statusText = when (feedback) {
        AttemptState.CORRECT -> "CORRECT"
        AttemptState.TOO_HIGH -> "TOO HIGH"
        AttemptState.TOO_LOW -> "TOO LOW"
        AttemptState.WRONG_NOTE -> if (timerEnabled && secondsLeft == 0) "TIME'S UP" else "TRY AGAIN"
        AttemptState.LISTENING -> if (running) "LISTENING" else "READY"
    }

    val statusColor = when (feedback) {
        AttemptState.CORRECT -> Lime
        AttemptState.TOO_HIGH, AttemptState.TOO_LOW -> Warning
        AttemptState.WRONG_NOTE -> Error
        AttemptState.LISTENING -> TextSecondary
    }

    Column(Modifier.fillMaxSize().background(Background)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("PRACTICE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
                    Text("${index + 1} / ${targets.size}", color = TextPrimary, style = MaterialTheme.typography.headlineSmall)
                }
                MetricChip("PTS", points.toString())
                Spacer(Modifier.width(7.dp))
                MetricChip("STREAK", streak.toString())
            }

            LinearProgressIndicator(
                progress = { (index.toFloat() / targets.size).coerceIn(0f, 1f) },
                Modifier.fillMaxWidth().height(4.dp),
                color = Lime,
                trackColor = Border
            )

            Surface(shape = RoundedCornerShape(16.dp), color = Surface, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("SPEED TIMER", color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (timerEnabled) "10 seconds · faster = more points" else "Relaxed mode · no deadline",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(if (timerEnabled) "${secondsLeft}s" else "OFF", color = Lime, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = timerEnabled, onCheckedChange = {
                        timerEnabled = it
                        if (it) secondsLeft = 10
                    })
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("FIND THIS NOTE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
                        Text(
                            if (current.fret == 0) "OPEN" else "FRET ${current.fret}",
                            color = Lime,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    AnimatedContent(
                        targetState = current,
                        transitionSpec = {
                            (fadeIn(tween(130)) + slideInVertically(tween(160)) { it / 4 })
                                .togetherWith(fadeOut(tween(90)))
                        },
                        label = "target"
                    ) { target ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${target.note.display}${target.octave}",
                                color = Lime,
                                style = MaterialTheme.typography.displayMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text("STRING ${target.stringNumber}", color = TextSecondary, style = MaterialTheme.typography.titleSmall)
                        }
                    }

                    Text(current.instruction, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                    Text("Cents do not block a correct note.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("TARGET POSITION", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            Text(
                                if (showTargetPosition) "Visual guide is on" else "Train from memory",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text("SHOW", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.width(6.dp))
                        Switch(checked = showTargetPosition, onCheckedChange = { showTargetPosition = it })
                    }
                    Box(
                        Modifier.fillMaxWidth().height(190.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Background)
                    ) {
                        Fretboard(
                            highlightedString = current.stringNumber,
                            highlightedFret = current.fret,
                            modifier = Modifier.fillMaxWidth()
                                .blur(if (showTargetPosition) 0.dp else 14.dp)
                        )
                        if (!showTargetPosition) {
                            Surface(
                                color = Background.copy(alpha = .78f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text(
                                    "POSITION HIDDEN",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(20.dp), color = Surface, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (running) "MIC ACTIVE" else "READY",
                                color = if (running) Lime else TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(statusText, color = statusColor, style = MaterialTheme.typography.titleMedium)
                        }
                        if (detected != null) {
                            Text(
                                "${detected!!.note.display}${detected!!.octave}",
                                color = TextPrimary,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                    PitchMeter(detected)
                    Text(
                        detected?.let {
                            "${it.frequencyHz.toInt()} Hz · ${if (it.cents >= 0) "+" else ""}${it.cents.toInt()}¢"
                        } ?: "Play one clean note · mute the other strings",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (feedback == AttemptState.CORRECT) {
                        Text("POINTS EARNED · SPEED BONUS APPLIED", color = Lime, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (denied) {
                Text("Microphone permission is required for practice.", color = Error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(4.dp))
        }

        Surface(color = Surface, tonalElevation = 6.dp) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            if (!running) startNewSession()
                        } else {
                            launcher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    enabled = !running,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (sessionStarted && !running) "NEW RANDOM SESSION" else "START PRACTICE")
                }
                if (sessionStarted && !running) {
                    Text(
                        "${points} points · ${accuracy}% accuracy · ${correct} correct",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = Surface) {
        Column(Modifier.padding(horizontal = 11.dp, vertical = 7.dp), horizontalAlignment = Alignment.End) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = Lime, style = MaterialTheme.typography.titleSmall)
        }
    }
}
