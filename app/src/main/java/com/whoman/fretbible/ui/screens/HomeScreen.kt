package com.whoman.fretbible.ui.screens
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.components.AnimatedBackground
import com.whoman.fretbible.ui.theme.*

@Composable
fun HomeScreen(onStartPractice: () -> Unit) {
    var appeared by remember { mutableStateOf(false) }
    val offset by animateDpAsState(if (appeared) 0.dp else 18.dp, spring(stiffness = Spring.StiffnessMediumLow), label = "homeOffset")
    LaunchedEffect(Unit) { appeared = true }
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground()
        Column(Modifier.fillMaxSize().padding(24.dp).offset(y = offset), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(Modifier.height(10.dp))
            Text("GOOD EVENING", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            Text("Your fretboard.", style = MaterialTheme.typography.headlineLarge)
            Text("Train recognition until the note feels obvious.", color = TextSecondary)
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ElevatedSurface), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TODAY'S PRACTICE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
                        Text("5 MIN", color = Lime, style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(8.dp)); Text("Find the Note", style = MaterialTheme.typography.headlineSmall)
                    Text("12 targets · live listening", color = TextSecondary); Spacer(Modifier.height(20.dp))
                    Button(onClick = onStartPractice, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("START PRACTICE") }
                }
            }
            Text("CONTINUE", color = TextMuted, style = MaterialTheme.typography.labelLarge)
            OutlinedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Natural Notes", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { 0.5f }, modifier = Modifier.fillMaxWidth(), color = Lime, trackColor = Border)
                    Spacer(Modifier.height(8.dp)); Text("12 / 24 exercises", color = TextSecondary)
                }
            }
        }
    }
}