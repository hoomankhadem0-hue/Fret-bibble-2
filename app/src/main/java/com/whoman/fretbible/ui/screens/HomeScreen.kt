package com.whoman.fretbible.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.components.AnimatedBackground
import com.whoman.fretbible.ui.theme.*
@Composable fun HomeScreen(onStartPractice:()->Unit){
var showAbout by rememberSaveable{mutableStateOf(true)}
Box(Modifier.fillMaxSize()){AnimatedBackground();Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Spacer(Modifier.height(10.dp));Text("GOOD EVENING",color=TextMuted,style=MaterialTheme.typography.labelLarge);Text("Your fretboard.",style=MaterialTheme.typography.headlineLarge);Text("Train recognition until the note feels obvious.",color=TextSecondary)
Card(shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=ElevatedSurface),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(24.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("TODAY'S PRACTICE",color=TextMuted,style=MaterialTheme.typography.labelLarge);Text("5 MIN",color=Lime,style=MaterialTheme.typography.labelLarge)};Spacer(Modifier.height(8.dp));Text("Find the Note",style=MaterialTheme.typography.headlineSmall);Text("12 targets · live guitar listening",color=TextSecondary);Spacer(Modifier.height(20.dp));Button(onClick=onStartPractice,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp)){Text("START PRACTICE")}}}
Text("CONTINUE",color=TextMuted,style=MaterialTheme.typography.labelLarge);OutlinedCard(shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp)){Text("Natural Notes",style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(8.dp));LinearProgressIndicator(progress={.5f},modifier=Modifier.fillMaxWidth(),color=Lime,trackColor=Border);Spacer(Modifier.height(8.dp));Text("12 / 24 exercises",color=TextSecondary)}}}
if(showAbout)AlertDialog(onDismissRequest={showAbout=false},containerColor=Surface,title={Text("FRET BIBLE",color=TextPrimary,style=MaterialTheme.typography.headlineSmall)},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Text("made by Hooman",color=Lime,style=MaterialTheme.typography.titleLarge);Text("A guitar-only fretboard recognition demo. It listens for one clean note at a time and advances when the pitch is stable.",color=TextSecondary);Text("This demo is optimized for guitar. Other instruments, voice and chords may be detected incorrectly.",color=TextMuted)}},confirmButton={Button(onClick={showAbout=false}){Text("GOT IT")}})}}
