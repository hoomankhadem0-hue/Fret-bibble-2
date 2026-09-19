package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.core.model.FretPosition
import com.whoman.fretbible.core.model.FretboardData
import com.whoman.fretbible.ui.theme.*

@Composable
fun FretboardExplorerScreen(onBack: () -> Unit) {
    val positions = FretboardData.all(24)
    val tuning = listOf("E4", "B3", "G3", "D3", "A2", "E2")

    Column(
        Modifier.fillMaxSize().background(Background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("FRETBOARD", color = TextMuted, style = MaterialTheme.typography.labelLarge)
                Text("Standard tuning · 24 frets", style = MaterialTheme.typography.headlineSmall)
            }
            TextButton(onClick = onBack) { Text("DONE") }
        }

        Text(
            "Swipe left / right. Every cell is a real fret position and shows its note.",
            color = TextSecondary
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Row(
                Modifier.fillMaxSize().horizontalScroll(rememberScrollState()).padding(12.dp)
            ) {
                Column(Modifier.width(78.dp)) {
                    Text(
                        "STRING",
                        color = TextMuted,
                        modifier = Modifier.height(54.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                    (1..6).forEach { stringNumber ->
                        Column(Modifier.height(68.dp)) {
                            Text("S$stringNumber", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(tuning[stringNumber - 1], color = Lime, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Column(Modifier.width(64.dp * 25)) {
                    Row(Modifier.height(54.dp)) {
                        (0..24).forEach { fret ->
                            Box(Modifier.width(64.dp).fillMaxHeight()) {
                                Text(
                                    if (fret == 0) "OPEN" else "F$fret",
                                    color = TextMuted,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    (1..6).forEach { stringNumber ->
                        Row(Modifier.height(68.dp)) {
                            FretboardRow(positions.filter { it.stringNumber == stringNumber })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FretboardRow(notes: List<FretPosition>) {
    notes.forEach { pos ->
        Box(
            Modifier.width(64.dp)
                .fillMaxHeight()
                .padding(3.dp)
                .background(if (pos.fret == 0) ElevatedSurface else Surface)
        ) {
            Column(
                Modifier.fillMaxSize().padding(6.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(pos.label, color = Lime, style = MaterialTheme.typography.titleMedium)
                Text(
                    "S" + pos.stringNumber + " · F" + pos.fret,
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
