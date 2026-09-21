package com.whoman.fretbible.ui.screens
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.analyzer.*
import com.whoman.fretbible.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun AnalyzerScreen(){
 val context=LocalContext.current
 val scope=rememberCoroutineScope()
 var fileName by remember{mutableStateOf<String?>(null)}
 var analyzing by remember{mutableStateOf(false)}
 var error by remember{mutableStateOf<String?>(null)}
 var result by remember{mutableStateOf<AnalyzerResult?>(null)}
 val launcher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
  if(uri!=null){fileName=uri.lastPathSegment?: "audio file";analyzing=true;error=null;result=null
   scope.launch{try{result=withContext(Dispatchers.Default){
    val d=AudioFileDecoder.decode(context,uri)
    val env=SignalFeatures.rmsEnvelope(d.samples)
    val bpm=MusicAnalyzer.estimateBpm(env,d.sampleRate,512)
    val frames=SignalFeatures.chromaFrames(d.samples,d.sampleRate)
    val aggregate=DoubleArray(12)
    for(frame in frames)for(pc in 0 until 12)aggregate[pc]+=frame[pc]
    val max=aggregate.maxOrNull()?:0.0
    if(max>0)for(pc in 0 until 12)aggregate[pc]/=max
    val key=MusicAnalyzer.estimateKey(aggregate)
    val chords=MusicAnalyzer.estimateChords(frames,d.sampleRate,2048,key=key.first)
    AnalyzerResult(bpm.first,bpm.second,key.first,key.second,chords,MusicAnalyzer.matchProgressions(chords,key.first))
   }}catch(t:Throwable){error=t.message?:"Could not analyze this audio file."}finally{analyzing=false}}
  }
 }
 Column(Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  Text("ANALYZER",color=Lime,style=MaterialTheme.typography.labelLarge)
  Text("Hear the song.",color=TextPrimary,style=MaterialTheme.typography.headlineLarge)
  Text("Upload a track and turn sound into useful guitar information.",color=TextSecondary)
  Card(colors=CardDefaults.cardColors(containerColor=ElevatedSurface),shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()){
   Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
    Text("TRACK",color=TextMuted,style=MaterialTheme.typography.labelLarge)
    Text(fileName?:"Choose an audio file",color=TextPrimary,style=MaterialTheme.typography.titleLarge)
    Text("Key · BPM · chord timeline",color=TextSecondary)
    Button(onClick={launcher.launch(arrayOf("audio/*"))},enabled=!analyzing,modifier=Modifier.fillMaxWidth()){Text(if(analyzing)"ANALYZING…" else "UPLOAD AUDIO")}
    error?.let{Text(it,color=Error,style=MaterialTheme.typography.bodySmall)}
    Text("Local processing · first 3 minutes · experimental",color=TextMuted,style=MaterialTheme.typography.bodySmall)
    Text("Harmonic knowledge base · ${ProgressionLibrary.patterns.size} progression patterns",color=Lime.copy(alpha=.75f),style=MaterialTheme.typography.bodySmall)
   }
  }
  result?.let{a->
   Text("RESULTS",color=TextMuted,style=MaterialTheme.typography.labelLarge)
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
    ResultCard("KEY",a.key,"${(a.keyConfidence*100).roundToInt()}%",Modifier.weight(1f))
    ResultCard("BPM","%.1f".format(a.bpm),"${(a.bpmConfidence*100).roundToInt()}%",Modifier.weight(1f))
   }
   Text("CHORD TIMELINE",color=TextMuted,style=MaterialTheme.typography.labelLarge)
   Surface(shape=RoundedCornerShape(18.dp),color=Surface,modifier=Modifier.fillMaxWidth()){
    Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
     a.chords.take(12).forEach{c->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(c.symbol,color=TextPrimary);Text(formatTime(c.startSeconds),color=TextSecondary)}}
    }
   }
   Text("ANALYSIS ROADMAP",color=TextMuted,style=MaterialTheme.typography.labelLarge)
   AnalyzerStep("01","KEY & SCALE","Detect the tonal center and likely scale.")
   AnalyzerStep("02","TEMPO","Estimate BPM and beat grid.")
   AnalyzerStep("03","CHORDS","Track chord changes across the song.")
   AnalyzerStep("04","GUITAR TAB","Experimental transcription from isolated or clear guitar parts.")
  }
 }
}
@Composable private fun AnalyzerStep(number:String,title:String,description:String){
 Surface(shape=RoundedCornerShape(16.dp),color=Surface,modifier=Modifier.fillMaxWidth()){
  Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)){
   Text(number,color=Lime,style=MaterialTheme.typography.titleLarge)
   Column(Modifier.weight(1f)){Text(title,color=TextPrimary,style=MaterialTheme.typography.titleLarge);Text(description,color=TextSecondary,style=MaterialTheme.typography.bodySmall)}
  }
 }
}
@Composable private fun ResultCard(label:String,value:String,confidence:String,modifier:Modifier){
 Surface(modifier=modifier,shape=RoundedCornerShape(18.dp),color=Surface){
  Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(label,color=TextMuted,style=MaterialTheme.typography.labelSmall);Text(value,color=TextPrimary,style=MaterialTheme.typography.titleLarge);Text(confidence,color=Lime,style=MaterialTheme.typography.labelSmall)}
 }
}
private fun formatTime(seconds:Double):String{val total=seconds.toInt().coerceAtLeast(0);return "%d:%02d".format(total/60,total%60)}
