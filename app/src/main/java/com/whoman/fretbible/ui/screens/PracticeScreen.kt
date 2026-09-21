package com.whoman.fretbible.ui.screens
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
        while (running && config.timerEnabled && !locked && secondsLeft > 0) { delay(1000); if (!locked) secondsLeft-- }
        if (running && config.timerEnabled && !locked && secondsLeft == 0) {
            feedback = AttemptState.WRONG_NOTE; misses++; streak = 0; engine.record(current, false); locked = true
            delay(300)
            if (index < targets.lastIndex) { index++; feedback = AttemptState.LISTENING; resetStability(); locked = false } else finish()
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
            val earned = 100 + if (config.timerEnabled) secondsLeft * 15 else 0 + streak * 25
            correct++; streak++; points += earned; engine.record(current, true); locked = true
            scope.launch {
                delay(500)
                if (index < targets.lastIndex) { index++; feedback = AttemptState.LISTENING; resetStability(); locked = false } else finish()
            }
        } else {
            misses++; streak = 0; engine.record(current, false); locked = true
            scope.launch { delay(300); feedback = AttemptState.LISTENING; resetStability(); locked = false }
        }
    }

    val status = when (feedback) {
        AttemptState.CORRECT -> "CORRECT"
        AttemptState.TOO_HIGH -> "TOO HIGH"
        AttemptState.TOO_LOW -> "TOO LOW"
        AttemptState.WRONG_NOTE -> if (config.timerEnabled && secondsLeft == 0) "TIME'S UP" else "TRY AGAIN"
        AttemptState.LISTENING -> if (running) "LISTENING" else "READY"
    }
    val statusColor = when (feedback) {
        AttemptState.CORRECT -> Lime
        AttemptState.TOO_HIGH, AttemptState.TOO_LOW -> Warning
        AttemptState.WRONG_NOTE -> Error
        AttemptState.LISTENING -> TextSecondary
    }

    Column(Modifier.fillMaxSize().background(Background)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp).blur(if (dialog.isNotEmpty()) 7.dp else 0.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("PRACTICE", color = Lime, style = MaterialTheme.typography.labelSmall)
                    Text(if (running) (index + 1).toString() + " / " + targets.size else "Session setup", style = MaterialTheme.typography.titleLarge)
                }
                MetricChip("PTS", points.toString()); Spacer(Modifier.width(7.dp)); MetricChip("STREAK", streak.toString())
            }

            if (!running) {
                Surface(shape = RoundedCornerShape(20.dp), color = ElevatedSurface, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("SESSION SETUP", color = Lime, style = MaterialTheme.typography.labelLarge)
                        SettingButton("QUESTIONS", config.count.toString()) { dialog = "count" }
                        Column(Modifier.fillMaxWidth()) {
                            Text("FRET RANGE", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            Text("0–" + config.maxFret + " frets", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(10.dp))
                            FretRangeSlider(
                                value = config.maxFret,
                                onValueChange = { config = config.copy(maxFret = it) },
                                onValueChangeFinished = { PracticePreferences.save(context, config) }
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("0", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                Text("12", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                Text("21", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        SettingButton("TRAINING MODE", config.mode.label) { dialog = "mode" }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("SPEED TIMER", style = MaterialTheme.typography.titleSmall); Text("10 seconds per target", color = TextMuted, style = MaterialTheme.typography.bodySmall) }
                            Switch(config.timerEnabled, { config = config.copy(timerEnabled = it); PracticePreferences.save(context, config) })
                        }
                    }
                }
            } else {
                LinearProgressIndicator(progress = { (index.toFloat() / targets.size).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(4.dp), color = Lime, trackColor = Border)
            }

            if (current != null) {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ElevatedSurface), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("FIND THIS NOTE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
                            if (running) Text(if (current.fret == 0) "OPEN" else "FRET " + current.fret, color = Lime, style = MaterialTheme.typography.labelSmall)
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                            Text(current.note.display + current.octave, color = Lime, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                            Text("STRING " + current.stringNumber, color = TextSecondary, style = MaterialTheme.typography.titleSmall)
                        }
                        Text("String " + current.stringNumber + " · Fret " + current.fret, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Surface(shape = RoundedCornerShape(20.dp), color = Surface, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("POSITION VISIBILITY", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                Text(if (hideTargetPosition) "Hidden · train from memory" else "Visible · use as a reference", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = hideTargetPosition,
                                onCheckedChange = { hideTargetPosition = it }
                            )
                        }
                        Box(Modifier.fillMaxWidth().height(164.dp).clip(RoundedCornerShape(15.dp)).background(Background)) {
                            Fretboard(
                                highlightedString = if (hideTargetPosition) null else current.stringNumber,
                                highlightedFret = if (hideTargetPosition) null else current.fret,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (hideTargetPosition) {
                                Surface(
                                    color = ElevatedSurface.copy(alpha = .92f),
                                    shape = RoundedCornerShape(999.dp),
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Text("POSITION HIDDEN", color = TextPrimary, style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp))
                                }
                            }
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(20.dp), color = Surface, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(if (running) "MIC ACTIVE" else "READY", color = if (running) Lime else TextMuted, style = MaterialTheme.typography.labelSmall)
                                Text(status, color = statusColor, style = MaterialTheme.typography.titleMedium)
                            }
                            if (detected != null) Text(detected!!.note.display + detected!!.octave, color = TextPrimary, style = MaterialTheme.typography.headlineSmall)
                        }
                        PitchMeter(detected)
                        Text(if (detected != null) detected!!.frequencyHz.toInt().toString() + " Hz · " + (if (detected!!.cents >= 0) "+" else "") + detected!!.cents.toInt() + "¢" else "Play one clean note · mute the other strings", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (denied) Text("Microphone permission is required for practice.", color = Error, style = MaterialTheme.typography.bodySmall)
        }

        Surface(color = Surface, tonalElevation = 6.dp) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Button(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) { if (!running) startNew() } else launcher.launch(Manifest.permission.RECORD_AUDIO)
                }, enabled = !running, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) { Text(if (sessionStarted && !running) "NEW SESSION" else "START PRACTICE") }
                if (sessionStarted && !running) Text(points.toString() + " points · " + accuracy + "% accuracy · " + correct + " correct", color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 7.dp))
            }
        }
    }

    if (dialog == "count") AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Questions") }, text = { Column { listOf(10, 20, 24, 30, 50).forEach { n -> TextButton(onClick = { config = config.copy(count = n); PracticePreferences.save(context, config); dialog = "" }) { Text(n.toString() + " questions") } } } }, confirmButton = {})
        if (dialog == "mode") AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Training mode") }, text = { Column { PracticeMode.entries.forEach { m -> TextButton(onClick = { config = config.copy(mode = m); PracticePreferences.save(context, config); dialog = "" }) { Text(m.label) } } } }, confirmButton = {})
}

@Composable private fun SettingButton(label: String, value: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall); Text(value, color = TextPrimary, style = MaterialTheme.typography.titleMedium) }
            Text("CHANGE", color = Lime, style = MaterialTheme.typography.labelSmall)
        }
    }
}
@Composable private fun MetricChip(label: String, value: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = Surface) { Column(Modifier.padding(horizontal = 11.dp, vertical = 7.dp), horizontalAlignment = Alignment.End) { Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall); Text(value, color = Lime, style = MaterialTheme.typography.titleSmall) } }
}


@Composable
private fun FretRangeSlider(value: Int, onValueChange: (Int) -> Unit, onValueChangeFinished: () -> Unit) {
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChange(it.toInt().coerceIn(1, 21)) },
        onValueChangeFinished = onValueChangeFinished,
        valueRange = 1f..21f,
        steps = 19,
        modifier = Modifier.fillMaxWidth()
    )
}
