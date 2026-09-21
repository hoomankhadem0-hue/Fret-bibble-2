package com.whoman.fretbible.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*

@Composable
fun RoadmapScreen() {
    val nodes = listOf(
        "Foundation" to "Six open strings, tuning and note names.",
        "Natural notes" to "Find A–G across the first positions.",
        "12th fret" to "Connect octave shapes and landmarks.",
        "Full fretboard" to "Recognize every note without counting.",
        "Intervals" to "See distance and relationships between notes.",
        "Scales" to "Turn the map into musical patterns."
    )
    val currentIndex = 0
    val completion = ((currentIndex + 1f) / nodes.size)

    Column(
        Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(
            kicker = "ROADMAP",
            title = "Learn the neck in layers.",
            subtitle = "A visual path from open strings to musical fluency."
        )

        SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(62.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = { completion }, color = Lime, trackColor = Border, strokeWidth = 6.dp)
                    Text("${currentIndex + 1}", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CURRENT STAGE", color = Lime, style = MaterialTheme.typography.labelMedium)
                    Text(nodes[currentIndex].first, style = MaterialTheme.typography.titleLarge)
                    Text("Start with open-string recognition.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            LinearProgressIndicator(
                progress = { completion },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Lime,
                trackColor = Border
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1 of ${nodes.size}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                Text("Foundation", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }

        SectionLabel("LEARNING PATH")
        nodes.forEachIndexed { index, node ->
            val active = index <= currentIndex
            val visible = remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible.value = true }
            val alpha by animateFloatAsState(if (visible.value) 1f else 0f, tween(260), label = "roadmapAlpha$index")

            Row(
                Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha },
                verticalAlignment = Alignment.Top
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(42.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = if (active) Lime else ElevatedSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Lime else Border)
                    ) {
                        Text(
                            "%02d".format(index + 1),
                            color = if (active) Background else TextMuted,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                        )
                    }
                    if (index != nodes.lastIndex) {
                        Box(Modifier.width(1.dp).height(58.dp).background(if (index < currentIndex) Lime.copy(alpha = .45f) else Border))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (active) ElevatedSurface else Surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Lime.copy(alpha = .17f) else Border),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(node.first, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(node.second, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            when {
                                index < currentIndex -> "DONE"
                                index == currentIndex -> "NOW"
                                else -> "SOON"
                            },
                            color = when {
                                active -> Lime
                                else -> TextMuted
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
