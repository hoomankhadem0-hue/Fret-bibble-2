package com.whoman.fretbible.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.theme.*
@Composable fun ProfileScreen(){Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Text("PROFILE",color=TextMuted,style=MaterialTheme.typography.labelLarge);Text("Who?man",style=MaterialTheme.typography.headlineLarge);Text("Electric guitar · Standard tuning",color=TextSecondary);HorizontalDivider(color=Border);Text("SETTINGS",color=TextMuted,style=MaterialTheme.typography.labelLarge);SettingsRow("Audio input","Microphone");SettingsRow("Practice tolerance","±35 cents");SettingsRow("Theme","Dark");HorizontalDivider(color=Border);Text("FRET BIBLE",color=Lime,style=MaterialTheme.typography.labelLarge);Text("made by Who?man",color=TextSecondary)}}
@Composable private fun SettingsRow(title:String,value:String){Row(Modifier.fillMaxWidth().padding(vertical=8.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(title);Text(value,color=TextSecondary)}}
