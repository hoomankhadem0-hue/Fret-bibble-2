package com.whoman.fretbible.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.components.*
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
        Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(
            kicker = "ROADMAP",
            title = "Learn the neck in layers.",
            subtitle = "Each stage adds one mental model without losing the previous one."
        )

        SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = LimeSoft) {
                    Text("1", color = Lime, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(12.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("CURRENT TRACK", color = Lime, style = MaterialTheme.typography.labelMedium)
                    Text("Foundation → Full Fretboard", style = MaterialTheme.typography.titleLarge)
                    Text("Practice recognition while the map grows.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        SectionLabel("THE PATH")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            nodes.forEachIndexed { index, item ->
                val active = index < 2
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(38.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = if (active) Lime else ElevatedSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Lime else Border)
                        ) {
                            Text(
                                item.first.takeLast(1),
                                color = if (active) Background else TextMuted,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(9.dp)
                            )
                        }
                        if (index != nodes.lastIndex) {
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.width(1.dp).height(48.dp).background(Border))
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(17.dp),
                        color = if (active) ElevatedSurface else Surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Lime.copy(alpha = .16f) else Border),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.second.first, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                                Text(item.second.second, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                if (active) "ACTIVE" else "LOCKED",
                                color = if (active) Lime else TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
