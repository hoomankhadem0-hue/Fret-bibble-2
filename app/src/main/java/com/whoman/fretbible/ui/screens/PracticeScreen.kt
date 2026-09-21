package com.whoman.fretbible.ui.screens

import android.Manifest
import android.media.AudioManager
import android.media.ToneGenerator
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
fun PracticeScreen(userName: String) {
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
    var secondsLeft by remember { mutableIntStateOf(config.timerSeconds ?: 0) }
    var countdown by remember { mutableStateOf<Int?>(null) }
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
        index = 0
        points = 0
        correct = 0
        misses = 0
        streak = 0
        secondsLeft = config.timerSeconds ?: 0
        feedback = AttemptState.LISTENING
        locked = false
        resetStability()
        sessionStarted = false
        sessionSaved = false
        sessionStartedAt = 0L
        countdown = 3
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        denied = !granted
        if (granted) startNew()
    }

    DisposableEffect(Unit) { onDispose { saveSession(); audio.stop() } }

    LaunchedEffect(countdown) {
        val start = countdown ?: return@LaunchedEffect
        var value = start
        while (value > 0 && countdown != null) {
            val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 72)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 90)
            delay(1000)
            tone.release()
            value--
            countdown = if (value > 0) value else null
        }
        if (start == 3 && countdown == null) {
            sessionStarted = true
            sessionSaved = false
            sessionStartedAt = System.currentTimeMillis()
            running = true
            scope.launch { audio.start() }
        }
    }

    LaunchedEffect(running, index, config.timerSeconds) {
        if (!running || !config.timerEnabled || current == null || locked) return@LaunchedEffect
        val limit = config.timerSeconds ?: return@LaunchedEffect
        secondsLeft = limit
        while (running && config.timerSeconds != null && !locked && secondsLeft > 0) {
            delay(1000)
            if (!locked) secondsLeft--
        }
        if (running && config.timerSeconds != null && !locked && secondsLeft == 0) {
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
            val earned = config.timerSeconds?.let { limit ->
                100 + ((120 - limit).coerceAtLeast(0) * 2) + streak * 25
            } ?: 0
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
        AttemptState.WRONG_NOTE -> if (config.timerSeconds != null && secondsLeft == 0) "TIME'S UP" else "TRY AGAIN"
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
                        Text(
                            if (running) "${userName.trim()} · Note recognition" else "Practice, ${userName.trim()}",
                            style = MaterialTheme.typography.titleLarge
                        )
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
                                    if (config.timerSeconds != null) "${secondsLeft}s" else "TRAINING",
                                    color = if (config.timerSeconds != null && secondsLeft <= 3) Error else Lime,
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

                        SettingRow(
                            "Timer",
                            config.timerSeconds?.let { "${it} sec · shorter = more points" } ?: "Off · training mode · no score"
                        ) { dialog = "timer" }

                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Speed timer", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    config.timerSeconds?.let { "${it} seconds per note" } ?: "Training mode · no score",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = config.timerSeconds != null,
                                onCheckedChange = {
                                    config = config.copy(timerSeconds = if (it) 10 else null)
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
                        enabled = !running && countdown == null,
                        modifier = Modifier.width(150.dp)
                    )
                }
            }
        }

        countdown?.let { value ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Background.copy(alpha = .94f)
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("GET READY", color = Lime, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(10.dp))
                    Text(value.toString(), color = TextPrimary, style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Your session starts now", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (dialog.isNotEmpty() && countdown == null) {
            Surface(
                Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = .46f)
            ) {}
            PracticeChoiceDialog(
                title = when (dialog) {
                    "count" -> "Questions"
                    "mode" -> "Training mode"
                    else -> "Timer"
                },
                subtitle = when (dialog) {
                    "count" -> "Choose the size of this session."
                    "mode" -> "Change how targets are selected."
                    else -> "Set a time limit or practice without one."
                },
                options = when (dialog) {
                    "count" -> listOf("10 questions" to 10, "20 questions" to 20, "24 questions" to 24, "30 questions" to 30, "50 questions" to 50)
                    "mode" -> PracticeMode.entries.map { it.label to it.ordinal }
                    else -> listOf(
                        "No timer · training · no score" to 0,
                        "3 seconds · high score" to 3,
                        "5 seconds" to 5,
                        "7 seconds" to 7,
                        "10 seconds" to 10,
                        "15 seconds" to 15,
                        "20 seconds" to 20,
                        "30 seconds" to 30,
                        "60 seconds" to 60,
                        "120 seconds" to 120
                    )
                },
                selected = when (dialog) {
                    "count" -> config.count
                    "mode" -> config.mode.ordinal
                    else -> config.timerSeconds ?: 0
                },
                onSelect = { value ->
                    when (dialog) {
                        "count" -> config = config.copy(count = value)
                        "mode" -> config = config.copy(mode = PracticeMode.entries[value])
                        else -> config = config.copy(timerSeconds = value.takeIf { it > 0 })
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
