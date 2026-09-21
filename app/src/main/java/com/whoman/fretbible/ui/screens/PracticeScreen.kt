package com.whoman.fretbible.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    var config by remember { mutableStateOf(PracticePreferences.load(context)) }
    var targets by remember { mutableStateOf(emptyList<com.whoman.fretbible.core.model.TargetNote>()) }
    var index by remember { mutableIntStateOf(0) }
    var points by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf(AttemptState.LISTENING) }
    var running by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(false) }
    var denied by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(10) }
    var stableFrames by remember { mutableIntStateOf(0) }
    var lastMidi by remember { mutableIntStateOf(-999) }
    var sessionStarted by remember { mutableStateOf(false) }
    var sessionSaved by remember { mutableStateOf(false) }
    var sessionStartedAt by remember { mutableLongStateOf(0L) }
    var hideTargetPosition by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf("") }

    val current = targets.getOrNull(index)
    val accuracy = if (correct + misses == 0) 0 else correct * 100 / (correct + misses)

    fun resetStability() { stableFrames = 0; lastMidi = -999 }
    fun saveSession() {
        if (!sessionStarted || sessionSaved) return
        val elapsed = if (sessionStartedAt > 0L) (System.currentTimeMillis() - sessionStartedAt) / 1000L else 0L
        PracticeStatsStore.saveSession(context, points, correct, correct + misses, elapsed)
        sessionSaved = true
    }
    fun finish() { running = false; audio.stop(); saveSession() }
    fun startNew() {
        PracticePreferences.save(context, config)
        targets = engine.newSession(config.count, config.maxFret, config.mode)
        index = 0; points = 0; correct = 0; misses = 0; streak = 0; secondsLeft = 10
        feedback = AttemptState.LISTENING; locked = false; resetStability()
        sessionStarted = true; sessionSaved = false; sessionStartedAt = System.currentTimeMillis(); running = true
        scope.launch { audio.start() }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        denied = !granted
        if (granted) startNew()
    }

    DisposableEffect(Unit) { onDispose { saveSession(); audio.stop() } }

    LaunchedEffect(running, index, config.timerEnabled) {
        if (!running || !config.timerEnabled || current == null || locked) return@LaunchedEffect
        secondsLeft = 10
        while (running && config.timerEnabled && !locked && secondsLeft > 0) {
            delay(1000)
            if (!locked) secondsLeft--
        }
        if (running && config.timerEnabled && !locked && secondsLeft == 0) {
            feedback = AttemptState.WRONG_NOTE
            misses++; streak = 0; engine.record(current, false); locked = true
            delay(350)
            if (index < targets.lastIndex) {
                index++; feedback = AttemptState.LISTENING; resetStability(); locked = false
            } else finish()
        }
    }

    LaunchedEffect(detected, index, running) {
        if (!running || current == null || detected == null || locked) return@LaunchedEffect
        val d = detected ?: return@LaunchedEffect
        if (d.confidence < 0.18) return@LaunchedEffect
        stableFrames = if (d.midi == lastMidi) stableFrames + 1 else 1
        lastMidi = d.midi
        if (stableFrames < 2) return@LaunchedEffect
        feedback = engine.evaluate(current, d)
        if (feedback == AttemptState.CORRECT) {
            val earned = 100 + (if (config.timerEnabled) secondsLeft * 15 else 0) + streak * 25
            correct++; streak++; points += earned; engine.record(current, true); locked = true
            scope.launch {
                delay(520)
                if (index < targets.lastIndex) {
                    index++; feedback = AttemptState.LISTENING; resetStability(); locked = false
                } else finish()
            }
        } else {
            misses++; streak = 0; engine.record(current, false); locked = true
            scope.launch { delay(320); feedback = AttemptState.LISTENING; resetStability(); locked = false }
        }
    }

    val status = when (feedback) {
        AttemptState.CORRECT -> "CORRECT"
        AttemptState.TOO_HIGH -> "TOO HIGH"
        AttemptState.TOO_LOW -> "TOO LOW"
        AttemptState.WRONG_NOTE -> if (config.timerEnabled && secondsLeft == 0) "TIME'S UP" else "TRY AGAIN"
        AttemptState.LISTENING -> if (running) "LISTENING" else "READY"
    }
    val statusColor by animateColorAsState(
        when (feedback) {
            AttemptState.CORRECT -> Lime
            AttemptState.TOO_HIGH, AttemptState.TOO_LOW -> Warning
            AttemptState.WRONG_NOTE -> Error
            AttemptState.LISTENING -> TextSecondary
        },
        label = "statusColor"
    )

    val totalTargets = targets.size.coerceAtLeast(1)
    val progress = ((index + if (feedback == AttemptState.CORRECT) 1 else 0).toFloat() / totalTargets).coerceIn(0f, 1f)

    Box(Modifier.fillMaxSize().background(Background)) {
        Column(
            Modifier
                .fillMaxSize()
                .blur(if (dialog.isNotEmpty()) 8.dp else 0.dp)
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("PRACTICE", color = Lime, style = MaterialTheme.typography.labelMedium)
                        Text(if (running) "Note recognition" else "Build a session", style = MaterialTheme.typography.titleLarge)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MetricPill("PTS", points.toString())
                        if (running) MetricPill("Q", "${index + 1}/${targets.size}", accent = false)
                    }
                }

                if (running) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ElevatedSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = .5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SESSION", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                Text(
                                    if (config.timerEnabled) "${secondsLeft}s" else "NO TIMER",
                                    color = if (config.timerEnabled && secondsLeft <= 3) Error else Lime,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(5.dp),
                                color = Lime,
                                trackColor = Border
                            )
                        }
                    }
                }

                if (!running) {
                    SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("SESSION SETUP", color = Lime, style = MaterialTheme.typography.labelMedium)
                                Text("Dial in the workout", style = MaterialTheme.typography.titleLarge)
                            }
                            Text("${config.count}", color = TextPrimary, style = MaterialTheme.typography.headlineSmall)
                        }

                        SettingRow("Questions", "${config.count} targets") { dialog = "count" }

                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                Column {
                                    Text("Fret range", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                                    Text("Open → fret ${config.maxFret}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                                Text("${config.maxFret}", color = Lime, style = MaterialTheme.typography.titleLarge)
                            }
                            Slider(
                                value = config.maxFret.toFloat(),
                                onValueChange = { config = config.copy(maxFret = it.toInt().coerceIn(1, 21)) },
                                onValueChangeFinished = { PracticePreferences.save(context, config) },
                                valueRange = 1f..21f,
                                steps = 19
                            )
                        }

                        SettingRow("Training mode", config.mode.label) { dialog = "mode" }

                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Speed timer", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                                Text("10 seconds per target", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = config.timerEnabled,
                                onCheckedChange = {
                                    config = config.copy(timerEnabled = it)
                                    PracticePreferences.save(context, config)
                                }
                            )
                        }
                    }
                }

                if (current != null) {
                    SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("FIND THIS NOTE", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                            if (running) {
                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("FRET", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        if (current.fret == 0) "OPEN" else current.fret.toString(),
                                        color = Lime,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.blur(if (hideTargetPosition && current.fret != 0) 6.dp else 0.dp)
                                    )
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                            Crossfade(targetState = current.note.display + current.octave, label = "targetNote") { value ->
                                Text(value, color = Lime, style = MaterialTheme.typography.displayMedium, modifier = Modifier.weight(1f))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("STRING", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                Text("${current.stringNumber}", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                        Text(
                            when {
                                hideTargetPosition -> "Play the note without using the position cue."
                                current.fret == 0 -> "Open string"
                                else -> "Position on the neck · fret ${current.fret}"
                            },
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("POSITION VISIBILITY", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                Text(
                                    if (hideTargetPosition) "Hidden · recall from memory" else "Visible · use as a reference",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Switch(
                                checked = hideTargetPosition,
                                onCheckedChange = { hideTargetPosition = it }
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(148.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(Background)
                        ) {
                            Fretboard(
                                highlightedString = if (hideTargetPosition) null else current.stringNumber,
                                highlightedFret = if (hideTargetPosition) null else current.fret,
                                maxFret = config.maxFret,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (hideTargetPosition) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = ElevatedSurface.copy(alpha = .94f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                                ) {
                                    Text(
                                        "POSITION HIDDEN",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(if (running) "MIC ACTIVE" else "READY", color = if (running) Lime else TextMuted, style = MaterialTheme.typography.labelSmall)
                                Text(status, color = statusColor, style = MaterialTheme.typography.titleLarge)
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = LimeSoft) {
                                Text(
                                    detected?.let { it.note.display + it.octave } ?: "—",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
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
                    }
                }

                if (denied) {
                    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Microphone permission is required for practice.", color = Error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(Modifier.height(6.dp))
            }

            Surface(
                color = SurfaceStrong,
                tonalElevation = 8.dp,
                shadowElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (sessionStarted && !running) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("LAST SESSION", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            Text("${points} pts · ${accuracy}% accuracy", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("READY WHEN YOU ARE", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            Text("Listen → find → play", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    PrimaryAction(
                        if (sessionStarted && !running) "NEW SESSION" else "START",
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                if (!running) startNew()
                            } else launcher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        enabled = !running,
                        modifier = Modifier.width(150.dp)
                    )
                }
            }
        }

        if (dialog.isNotEmpty()) {
            Surface(
                Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = .46f)
            ) {}
            PracticeChoiceDialog(
                title = if (dialog == "count") "Questions" else "Training mode",
                subtitle = if (dialog == "count") "Choose the size of this session." else "Change how targets are selected.",
                options = if (dialog == "count") {
                    listOf("10 questions" to 10, "20 questions" to 20, "24 questions" to 24, "30 questions" to 30, "50 questions" to 50)
                } else {
                    PracticeMode.entries.map { it.label to it.ordinal }
                },
                selected = if (dialog == "count") config.count else config.mode.ordinal,
                onSelect = { value ->
                    if (dialog == "count") {
                        config = config.copy(count = value)
                    } else {
                        config = config.copy(mode = PracticeMode.entries[value])
                    }
                    PracticePreferences.save(context, config)
                    dialog = ""
                },
                onDismiss = { dialog = "" }
            )
        }
    }
}

@Composable
private fun PracticeChoiceDialog(
    title: String,
    subtitle: String,
    options: List<Pair<String, Int>>,
    selected: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            shape = RoundedCornerShape(24.dp),
            color = ElevatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, style = MaterialTheme.typography.headlineSmall)
                        Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = onDismiss) { Text("CLOSE", color = TextMuted) }
                }
                options.forEach { option ->
                    val selectedNow = option.second == selected
                    Surface(
                        onClick = { onSelect(option.second) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selectedNow) LimeSoft else Surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedNow) Lime.copy(alpha = .4f) else Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(option.first, color = if (selectedNow) TextPrimary else TextSecondary, style = MaterialTheme.typography.bodyLarge)
                            Text(if (selectedNow) "●" else "○", color = if (selectedNow) Lime else TextMuted, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
