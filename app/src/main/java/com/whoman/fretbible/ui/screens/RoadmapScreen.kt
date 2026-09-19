package com.whoman.fretbible.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.theme.*

@Composable fun RoadmapScreen(){
    val nodes=listOf("Start" to "Foundation","Open Strings" to "Hear every string","Natural Notes" to "Map the fretboard","12th Fret" to "See the octave","Full Fretboard" to "Instant recognition","Intervals" to "Think in distance")
    Column(Modifier.fillMaxSize().padding(24.dp)){Text("ROADMAP",color=TextMuted,style=MaterialTheme.typography.labelLarge);Text("Your path",style=MaterialTheme.typography.headlineLarge);Spacer(Modifier.height(22.dp));nodes.forEachIndexed{i,n->Row(Modifier.fillMaxWidth().padding(vertical=10.dp)){Text(if(i<2)"●" else "○",color=if(i<2)Lime else TextMuted);Spacer(Modifier.width(18.dp));Column{Text(n.first,style=MaterialTheme.typography.titleLarge);Text(n.second,color=TextSecondary)}}}}
}