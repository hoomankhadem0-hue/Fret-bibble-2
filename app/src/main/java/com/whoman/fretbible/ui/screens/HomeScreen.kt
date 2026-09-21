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
import com.whoman.fretbible.ui.components.AnimatedBackground
import com.whoman.fretbible.ui.theme.*

@Composable
fun HomeScreen(onStartPractice: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground()
        Column(
            Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(6.dp))
            Text("GOOD EVENING", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            Text("Know the neck.", style = MaterialTheme.typography.headlineLarge)
            Text("Learn every note by playing it — not by memorizing a picture.", color = TextSecondary)

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TODAY'S PRACTICE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
                        Text("24 POSITIONS", color = Lime, style = MaterialTheme.typography.labelLarge)
                    }
                    Text("Find the Note", style = MaterialTheme.typography.headlineSmall)
                    Text("Random targets across the complete fretboard.", color = TextSecondary)
                    Button(
                        onClick = onStartPractice,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("START PRACTICE") }
                }
            }

            Text("YOUR FRETBOARD", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(vertical = 14.dp)) {
                    Text(
                        "Standard tuning · notes 0–12 · swipe horizontally",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    HomeFretboard()
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeStat("OPEN", "E A D G B E", Modifier.weight(1f))
                HomeStat("RANGE", "24 frets", Modifier.weight(1f))
            }

            Text("HOW IT WORKS", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            Surface(shape = RoundedCornerShape(16.dp), color = Surface, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("1  See a target", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text("2  Play one clean note", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Text("3  Get points and move on", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HomeFretboard() {
    val positions = remember { FretboardData.all(12) }
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp)
    ) {
        Column(Modifier.width(48.dp)) {
            Spacer(Modifier.height(26.dp))
            (1..6).forEach { stringNumber ->
                Row(Modifier.height(42.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("S$stringNumber", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Column {
            Row(Modifier.height(26.dp)) {
                (0..12).forEach { fret ->
                    Text(if (fret == 0) "O" else "$fret", color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(52.dp).padding(start = 5.dp))
                }
            }
            (1..6).forEach { stringNumber ->
                Row(Modifier.height(42.dp)) {
                    positions.filter { it.stringNumber == stringNumber }.forEach { pos ->
                        Box(
                            Modifier.width(52.dp).fillMaxHeight().padding(2.dp)
                                .background(if (pos.fret == 0) ElevatedSurface else Background, RoundedCornerShape(6.dp))
                        ) {
                            Column(Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.Center) {
                                Text(pos.note.display, color = if (pos.fret == 0) Lime else TextPrimary,
                                    style = MaterialTheme.typography.labelLarge)
                                Text("${pos.octave}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = Surface) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }
    }
}
