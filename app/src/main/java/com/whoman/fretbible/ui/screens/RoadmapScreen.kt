package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
        Triple("01","Foundation","Six open strings"),
        Triple("02","Natural Notes","Find notes without guessing"),
        Triple("03","12th Fret","Understand the octave"),
        Triple("04","Full Fretboard","Recognize every position"),
        Triple("05","Intervals","See distance between notes"),
        Triple("06","Scales","Turn the map into music")
    )

    Column(
        Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader("ROADMAP","Learn the neck in layers.","One skill at a time. Each stage reinforces the last.")

        SurfaceCard(Modifier.fillMaxWidth(), elevated = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(CircleShape, color = LimeSoft) {
                    Text("02", color = Lime, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(11.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("CURRENT PATH", color = Lime, style = MaterialTheme.typography.labelMedium)
                    Text("Natural Notes", style = MaterialTheme.typography.titleLarge)
                    Text("Build recall before expanding the map.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        SectionLabel("LEARNING PATH")
        nodes.forEachIndexed { i, node ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(CircleShape, color = if(i < 2) Lime else ElevatedSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if(i < 2) Lime else Border)) {
                        Text(node.first, color = if(i < 2) Background else TextMuted,
                            style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal=8.dp, vertical=9.dp))
                    }
                    if(i < nodes.lastIndex) Box(Modifier.width(1.dp).height(55.dp).background(Border))
                }
                Spacer(Modifier.width(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if(i < 2) ElevatedSurface else Surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if(i < 2) Lime.copy(alpha=.18f) else Border),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(node.second, color=TextPrimary, style=MaterialTheme.typography.titleMedium)
                            Text(node.third, color=TextSecondary, style=MaterialTheme.typography.bodySmall)
                        }
                        Text(if(i < 2) "IN PROGRESS" else "NEXT", color=if(i<2) Lime else TextMuted, style=MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}