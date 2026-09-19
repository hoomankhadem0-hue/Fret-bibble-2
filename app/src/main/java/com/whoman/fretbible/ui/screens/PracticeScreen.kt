package com.whoman.fretbible.ui.screens
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.whoman.fretbible.audio.AudioEngine
import com.whoman.fretbible.practice.*
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PracticeScreen(){
val context=LocalContext.current;val scope=rememberCoroutineScope();val audio=remember{AudioEngine()};val engine=remember{PracticeEngine()};val detected by audio.detected.collectAsState();val targets=remember{engine.targets()}
var index by remember{mutableIntStateOf(0)};var wrong by remember{mutableIntStateOf(0)};var correct by remember{mutableIntStateOf(0)};var feedback by remember{mutableStateOf(AttemptState.LISTENING)};var running by remember{mutableStateOf(false)};var denied by remember{mutableStateOf(false)};var elapsed by remember{mutableIntStateOf(0)}
var stableFrames by remember{mutableIntStateOf(0)};var lastMidi by remember{mutableIntStateOf(-999)};var lastCents by remember{mutableFloatStateOf(999f)};var locked by remember{mutableStateOf(false)}
fun resetStability(){stableFrames=0;lastMidi=-999;lastCents=999f}
val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->denied=!granted;if(granted){running=true;scope.launch{audio.start()}}}
DisposableEffect(Unit){onDispose{audio.stop()}}
LaunchedEffect(running){if(running)while(running){delay(1000);elapsed++}}
LaunchedEffect(detected,index,running){
if(!running||detected==null||index>=targets.size||locked)return@LaunchedEffect
val d=detected!!;if(d.confidence<.78)return@LaunchedEffect
val same=d.midi==lastMidi&&abs(d.cents-lastCents)<35f
stableFrames=if(same)stableFrames+1 else 1;lastMidi=d.midi;lastCents=d.cents.toFloat()
if(stableFrames<2)return@LaunchedEffect
val state=engine.evaluate(targets[index],d);if(state==AttemptState.LISTENING)return@LaunchedEffect
feedback=state
if(state==AttemptState.CORRECT){correct++;locked=true;delay(650);if(index<targets.lastIndex){index++;feedback=AttemptState.LISTENING;resetStability();locked=false}else{running=false;audio.stop()}}
else{wrong++;locked=true;delay(350);feedback=AttemptState.LISTENING;resetStability();locked=false}}
val current=targets[index.coerceIn(0,targets.lastIndex)];val accuracy=if(correct+wrong==0)0 else correct*100/(correct+wrong)
val statusText=when(feedback){AttemptState.CORRECT->"NICE — NEXT NOTE";AttemptState.TOO_HIGH->"TOO HIGH — LOWER THE PITCH";AttemptState.TOO_LOW->"TOO LOW — RAISE THE PITCH";AttemptState.WRONG_NOTE->"WRONG NOTE — TRY AGAIN";AttemptState.LISTENING->"LISTENING FOR A CLEAN NOTE"}
val statusColor=when(feedback){AttemptState.CORRECT->Lime;AttemptState.TOO_HIGH,AttemptState.TOO_LOW->Warning;AttemptState.WRONG_NOTE->Error;AttemptState.LISTENING->TextSecondary}
Column(Modifier.fillMaxSize().padding(horizontal=18.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("TARGET ${(index+1).coerceAtMost(targets.size)} / ${targets.size}",color=TextSecondary);Text("%02d:%02d".format(elapsed/60,elapsed%60),color=TextSecondary)}
AnimatedContent(targetState=current,transitionSpec={(fadeIn(tween(180))+slideInVertically(tween(220)){it/3}).togetherWith(fadeOut(tween(120)))},label="target"){target->Column{Text("PLAY THIS NOTE",color=TextMuted,style=MaterialTheme.typography.labelLarge);Text(target.note.display+target.octave,color=Lime,style=MaterialTheme.typography.displayMedium);Text(target.instruction,color=TextPrimary,style=MaterialTheme.typography.titleLarge);Text("Find it at fret ${target.fret} · string ${target.stringNumber}",color=TextMuted,style=MaterialTheme.typography.labelLarge)}}
Fretboard(highlightedString=current.stringNumber, highlightedFret=current.fret, modifier=Modifier.fillMaxWidth())
Card(shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Surface),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(if(running)"MIC • GUITAR LISTENING" else "READY",color=statusColor,style=MaterialTheme.typography.labelLarge);Text("ACC ${accuracy}%",color=TextMuted,style=MaterialTheme.typography.labelLarge)};PitchMeter(detected);Text(statusText,color=statusColor,style=MaterialTheme.typography.titleLarge);if(running)Text("Play one clean note. Other strings should be muted.",color=TextMuted,style=MaterialTheme.typography.bodyMedium)}}
if(denied)Text("Microphone permission is required for live note detection.",color=Error)
Spacer(Modifier.weight(1f))
if(!running&&index>=targets.lastIndex)Card(colors=CardDefaults.cardColors(containerColor=ElevatedSurface)){Column(Modifier.padding(20.dp)){Text("SESSION COMPLETE",color=Lime,style=MaterialTheme.typography.labelLarge);Text("${accuracy}% accuracy",style=MaterialTheme.typography.headlineSmall);Text("${elapsed} seconds · ${correct} correct",color=TextSecondary)}}else Button(onClick={if(ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){running=true;scope.launch{audio.start()}}else launcher.launch(Manifest.permission.RECORD_AUDIO)},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(14.dp)){Text(if(running)"LISTENING…" else "START LISTENING")}}
}