package com.whoman.fretbible.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.audio.AudioSettings
import com.whoman.fretbible.ui.theme.*
@Composable fun ProfileScreen(onAnalyzer:()->Unit){
 val context=androidx.compose.ui.platform.LocalContext.current
 var sensitivity by remember{mutableFloatStateOf(0.00035f)}
 LaunchedEffect(Unit){AudioSettings.load(context);sensitivity=AudioSettings.sensitivity}
 Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  Text("MORE",color=TextMuted,style=MaterialTheme.typography.labelLarge)
  Text("Tools & Settings",style=MaterialTheme.typography.headlineLarge)
  Text("Everything outside the main learning flow, in one place.",color=TextSecondary)
  Surface(shape=RoundedCornerShape(18.dp),color=ElevatedSurface,modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
   Text("ANALYZER",color=Lime,style=MaterialTheme.typography.labelLarge);Text("Song analysis",style=MaterialTheme.typography.titleLarge);Text("Key, BPM, chords and progression analysis.",color=TextSecondary)
   OutlinedButton(onClick=onAnalyzer,modifier=Modifier.fillMaxWidth()){Text("OPEN ANALYZER")}
  }}
  Text("AUDIO",color=TextMuted,style=MaterialTheme.typography.labelLarge);Text("Microphone sensitivity",style=MaterialTheme.typography.titleLarge);Text("Controls how quiet a note can be before the detector ignores it.",color=TextSecondary)
  Slider(value=sensitivity,onValueChange={sensitivity=it;AudioSettings.setSensitivity(context,it)},valueRange=0.00005f..0.006f,steps=23)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("MORE SENSITIVE",color=Lime,style=MaterialTheme.typography.labelSmall);Text("%.5f".format(sensitivity),color=TextMuted,style=MaterialTheme.typography.labelSmall);Text("LESS SENSITIVE",color=TextMuted,style=MaterialTheme.typography.labelSmall)}
  HorizontalDivider(color=Border);Text("PRACTICE",color=TextMuted,style=MaterialTheme.typography.labelLarge);SettingsRow("Pitch matching","Note + octave");SettingsRow("Tolerance","Cents do not block a correct note");SettingsRow("Tuning","Standard E A D G B E");Text("Practice mode, target count and fret range are configured at the start of each session.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
  HorizontalDivider(color=Border);Text("FRET BIBLE",color=Lime,style=MaterialTheme.typography.labelLarge);Text("made by Hooman",color=TextSecondary)
 }}
@Composable private fun SettingsRow(title:String,value:String){Row(Modifier.fillMaxWidth().padding(vertical=8.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(title);Text(value,color=TextSecondary)}}
