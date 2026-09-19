package com.whoman.fretbible.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.theme.*

@Composable fun ProgressScreen(){Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){Text("PROGRESS",color=TextMuted,style=MaterialTheme.typography.labelLarge);Text("Getting sharper.",style=MaterialTheme.typography.headlineLarge);Stat("ACCURACY","84%",.84f);Stat("PRACTICE TIME","2h 18m",.46f);Stat("NOTES MASTERED","31",.62f);Spacer(Modifier.height(8.dp));Text("FOCUS",color=TextMuted,style=MaterialTheme.typography.labelLarge);Text("Keep training the notes that slow you down.",color=TextSecondary)}}
@Composable private fun Stat(label:String,value:String,progress:Float){Card(colors=CardDefaults.cardColors(containerColor=Surface)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label,color=TextMuted,style=MaterialTheme.typography.labelLarge);Text(value,color=Lime,style=MaterialTheme.typography.headlineSmall)};LinearProgressIndicator(progress={progress},modifier=Modifier.fillMaxWidth(),color=Lime,trackColor=Border)}}}
