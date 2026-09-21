package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.practice.PracticeStatsStore
import com.whoman.fretbible.ui.theme.*

@Composable
fun ProgressScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var stats by remember { mutableStateOf(PracticeStatsStore.load(context)) }

    LaunchedEffect(Unit) {
        stats = PracticeStatsStore.load(context)
    }

    val minutes = stats.practiceSeconds / 60
    val hours = minutes / 60
    val mins = minutes % 60

    Column(
        Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("PROGRESS", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        Text("Your practice history", style = MaterialTheme.typography.headlineLarge)
        Text(
            if (stats.sessions == 0) "Complete your first session to start building your stats."
            else "Every completed session is saved on this device.",
            color = TextSecondary
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("SESSIONS", stats.sessions.toString(), Modifier.weight(1f))
            StatCard("POINTS", stats.points.toString(), Modifier.weight(1f))
        }

        StatCard("OVERALL ACCURACY", "${stats.accuracy}%", Modifier.fillMaxWidth())

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("CORRECT", stats.correct.toString(), Modifier.weight(1f))
            StatCard("BEST SESSION", stats.bestSessionPoints.toString(), Modifier.weight(1f))
        }

        StatCard(
            "PRACTICE TIME",
            if (hours > 0) "${hours}h ${mins}m" else "${mins}m",
            Modifier.fillMaxWidth()
        )

        Text("HOW IT'S CALCULATED", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        Surface(shape = RoundedCornerShape(18.dp), color = Surface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Accuracy", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("Correct notes ÷ all recorded attempts.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Text("Points", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("Base points + speed bonus + streak bonus.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Text("Data stays on this device.", color = Lime, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = Surface) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = Lime, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
