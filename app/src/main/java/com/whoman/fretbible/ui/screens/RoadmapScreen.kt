package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.theme.*

@Composable
fun RoadmapScreen() {
    val nodes = listOf(
        "01" to ("Foundation" to "Learn the six open strings"),
        "02" to ("Natural Notes" to "Find notes without guessing"),
        "03" to ("12th Fret" to "Understand the octave"),
        "04" to ("Full Fretboard" to "Recognize every position"),
        "05" to ("Intervals" to "See distance between notes"),
        "06" to ("Scales" to "Turn the map into music")
    )

    Column(
        Modifier.fillMaxSize().background(Background).padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text("ROADMAP", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        Text("Your path through the neck", color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
        Text(
            "Build recognition from open strings to the full fretboard.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
        )

        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            nodes.forEachIndexed { index, item ->
                val unlocked = index < 2
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (unlocked) ElevatedSurface else Surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (unlocked) Lime.copy(alpha = .14f) else Background
                        ) {
                            Text(
                                item.first,
                                color = if (unlocked) Lime else TextMuted,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(Modifier.weight(1f)) {
                            Text(item.second.first, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(item.second.second, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }

                        Text(
                            if (unlocked) "ACTIVE" else "LOCKED",
                            color = if (unlocked) Lime else TextMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
