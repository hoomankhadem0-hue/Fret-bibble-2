package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.practice.PracticeStatsStore
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*

@Composable
fun HomeScreen(
    onStartPractice: () -> Unit,
    onOpenDictionary: () -> Unit,
    onOpenAnalyzer: () -> Unit
) {
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
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

            SectionLabel("LEARN")
            ToolCard(
                title = "Fretboard Dictionary",
                description = "Browse every note, string and fret in standard tuning.",
                meta = "OPEN → FRET 21",
                onClick = onOpenDictionary
            )
            ToolCard(
                title = "Song Analyzer",
                description = "Turn a track into key, tempo and harmonic landmarks.",
                meta = "AUDIO → ANALYSIS",
                onClick = onOpenAnalyzer
            )

            SectionLabel("THE LOOP")
            SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                LoopStep("01", "See", "A note target appears.")
                LoopStep("02", "Play", "Play one clean note.")
                LoopStep("03", "Learn", "Get feedback and build recall.")
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ToolCard(
    title: String,
    description: String,
    meta: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ElevatedSurface.copy(alpha = .92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = .78f))
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = RoundedCornerShape(13.dp), color = LimeSoft) {
                Text("↗", color = Lime, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(description, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Text(meta, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }
            Text("›", color = Lime, style = MaterialTheme.typography.headlineSmall)
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
