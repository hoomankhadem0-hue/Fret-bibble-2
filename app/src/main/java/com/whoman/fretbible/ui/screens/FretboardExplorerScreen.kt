package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.core.model.FretPosition
import com.whoman.fretbible.core.model.FretboardData
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*

@Composable
fun FretboardExplorerScreen(onBack: () -> Unit) {
    val maxFret = 21
    val positions = FretboardData.all(maxFret)
    val tuning = listOf("E4", "B3", "G3", "D3", "A2", "E2")

    Column(
        Modifier.fillMaxSize().background(Background).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(
            kicker = "NOTE DICTIONARY",
            title = "Fretboard dictionary.",
            subtitle = "Standard tuning · open string to fret $maxFret",
            action = { TextButton(onClick = onBack) { Text("DONE", color = TextSecondary) } }
        )

        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("NOTE MAP", color = Lime, style = MaterialTheme.typography.labelMedium)
                Text("Swipe →", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }
            Text("Every cell is a real position, with string and fret context.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }

        SurfaceCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Row(
                Modifier.fillMaxSize().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Column(Modifier.width(72.dp)) {
                    Spacer(Modifier.height(46.dp))
                    tuning.forEachIndexed { index, tuningName ->
                        Column(Modifier.height(64.dp), verticalArrangement = Arrangement.Center) {
                            Text("S${index + 1}", color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                            Text(tuningName, color = Lime, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Column(Modifier.width(60.dp * (maxFret + 1))) {
                    Row(Modifier.height(46.dp)) {
                        (0..maxFret).forEach { fret ->
                            Box(Modifier.width(60.dp).fillMaxHeight()) {
                                Text(
                                    if (fret == 0) "OPEN" else "F$fret",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    (1..6).forEach { stringNumber ->
                        Row(Modifier.height(64.dp)) {
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
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (pos.fret == 0) ElevatedSurface else Background,
            border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = .55f)),
            modifier = Modifier.width(60.dp).height(58.dp).padding(3.dp)
        ) {
            Column(
                Modifier.fillMaxSize().padding(6.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(pos.label, color = if (pos.fret == 0) Lime else TextPrimary, style = MaterialTheme.typography.titleSmall)
                Text(
                    "S${pos.stringNumber} · F${pos.fret}",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
