package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.core.model.FretboardData
import com.whoman.fretbible.practice.PracticeStatsStore
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*

@Composable
fun HomeScreen(onStartPractice: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val stats = remember { PracticeStatsStore.load(context) }

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground()
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader(
                kicker = "FRET BIBLE",
                title = "Know the neck.",
                subtitle = "Learn every note by hearing it, finding it, and playing it."
            )

            SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("NEXT SESSION", color = Lime, style = MaterialTheme.typography.labelMedium)
                        Text("Find the Note", style = MaterialTheme.typography.headlineSmall)
                        Text("Random targets, up to 21 frets.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                    Surface(shape = RoundedCornerShape(14.dp), color = LimeSoft) {
                        Text("●", color = Lime, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(10.dp))
                    }
                }
                Spacer(Modifier.height(2.dp))
                PrimaryAction("START PRACTICE", onStartPractice, Modifier.fillMaxWidth())
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat("SESSIONS", stats.sessions.toString(), Modifier.weight(1f))
                MiniStat("ACCURACY", "${stats.accuracy}%", Modifier.weight(1f), accent = true)
                MiniStat("POINTS", stats.points.toString(), Modifier.weight(1f))
            }

            SectionLabel("THE NECK")
            SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Standard tuning", style = MaterialTheme.typography.titleMedium)
                        Text("Open strings → fret 12 · swipe to explore", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("E A D G B E", color = Lime, style = MaterialTheme.typography.labelMedium)
                }
                HomeFretboard()
            }

            SectionLabel("THE LOOP")
            SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                LoopStep("01", "See", "A note target appears.")
                LoopStep("02", "Play", "Play one clean note.")
                LoopStep("03", "Learn", "Get feedback and build recall.")
            }

            SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("BUILT FOR CONSISTENCY", color = Lime, style = MaterialTheme.typography.labelMedium)
                        Text("Short sessions. Real repetition. Less staring at diagrams.", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("21", color = TextPrimary, style = MaterialTheme.typography.headlineSmall)
                }
                Text("Maximum fret range", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LoopStep(number: String, title: String, description: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(10.dp), color = LimeSoft) {
            Text(number, color = Lime, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
            Text(description, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun HomeFretboard() {
    val positions = remember { FretboardData.all(12) }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 2.dp)) {
        Column(Modifier.width(44.dp)) {
            Spacer(Modifier.height(24.dp))
            (1..6).forEach { stringNumber ->
                Row(Modifier.height(39.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("S$stringNumber", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Column {
            Row(Modifier.height(24.dp)) {
                (0..12).forEach { fret ->
                    Text(
                        if (fret == 0) "O" else "$fret",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(50.dp).padding(start = 4.dp)
                    )
                }
            }
            (1..6).forEach { stringNumber ->
                Row(Modifier.height(39.dp)) {
                    positions.filter { it.stringNumber == stringNumber }.forEach { pos ->
                        Box(
                            Modifier
                                .width(50.dp)
                                .fillMaxHeight()
                                .padding(2.dp)
                                .background(
                                    if (pos.fret == 0) ElevatedSurface else Background,
                                    RoundedCornerShape(7.dp)
                                )
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(3.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(pos.note.display, color = if (pos.fret == 0) Lime else TextPrimary, style = MaterialTheme.typography.labelLarge)
                                Text("${pos.octave}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
