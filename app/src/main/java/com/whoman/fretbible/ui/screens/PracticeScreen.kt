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
import com.whoman.fretbible.practice.AttemptState
import com.whoman.fretbible.practice.PracticeEngine
import com.whoman.fretbible.ui.components.Fretboard
import com.whoman.fretbible.ui.components.PitchMeter
import com.whoman.fretbible.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PracticeScreen() {
    val context=LocalContext.current; val scope=rememberCoroutineScope(); val audio=remember{AudioEngine()}; val engine=remember{PracticeEngine()}
    val detected by audio.detected.collectAsState(); val targets=remember{engine.targets()}
    var index by remember{mutableIntStateOf(0)}; var attempts by remember{mutableIntStateOf(0)}; var correct by remember{mutableIntStateOf(0)}
    var feedback by remember{mutableStateOf(AttemptState.LISTENING)}; var running by remember{mutableStateOf(false)}; var denied by remember{mutableStateOf(false)}; var elapsed by remember{mutableIntStateOf(0)}
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->denied=!granted;if(granted){running=true;scope.launch{audio.start()}}}
    DisposableEffect(Unit){onDispose{audio.stop()}}
    LaunchedEffect(running){if(running)while(running){delay(1000);elapsed++}}
    LaunchedEffect(detected,index,running){
        if(!running||detected==null||index>=targets.size)return@LaunchedEffect
        val state=engine.evaluate(targets[index],detected!!); feedback=state; attempts++
        if(state==AttemptState.CORRECT){correct++;delay(480);if(index<targets.lastIndex){index++;feedback=AttemptState.LISTENING}else{running=false;audio.stop()}}
    }
    val current=targets[index.coerceIn(0,targets.lastIndex)]; val accuracy=if(attempts==0)0 else correct*100/attempts
    val text=when(feedback){AttemptState.CORRECT->"NICE";AttemptState.TOO_HIGH->"TOO HIGH";AttemptState.TOO_LOW->"TOO LOW";AttemptState.WRONG_NOTE->"WRONG NOTE";AttemptState.LISTENING->"PLAY A SINGLE NOTE"}
    val color=when(feedback){AttemptState.CORRECT->Lime;AttemptState.TOO_HIGH,AttemptState.TOO_LOW->Warning;AttemptState.WRONG_NOTE->Error;AttemptState.LISTENING->TextSecondary}
    Column(Modifier.fillMaxSize().padding(horizontal=18.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text((index+1).coerceAtMost(targets.size).toString()+" / "+targets.size,color=TextSecondary);Text("%02d:%02d".format(elapsed/60,elapsed%60),color=TextSecondary)}
        AnimatedContent(targetState=current.note,transitionSpec={(fadeIn(tween(180))+slideInVertically(tween(220)){it/3}).togetherWith(fadeOut(tween(120)))},label="targetNote"){note->Column{Text("FIND",color=TextMuted,style=MaterialTheme.typography.labelLarge);Text(note.display,color=Lime,style=MaterialTheme.typography.displayMedium);Text("ANY POSITION",color=TextSecondary,style=MaterialTheme.typography.labelLarge)}}
        Fretboard(highlightedString=null,highlightedFret=null,modifier=Modifier.fillMaxWidth())
        Card(shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Surface),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(if(running)"LISTENING" else "READY",color=color,style=MaterialTheme.typography.labelLarge);Text("ACC "+accuracy+"%",color=TextMuted,style=MaterialTheme.typography.labelLarge)};PitchMeter(detected);Text(text,color=color,style=MaterialTheme.typography.titleLarge)}}
        if(denied)Text("Microphone permission is required for live note detection.",color=Error)
        Spacer(Modifier.weight(1f))
        if(!running&&index>=targets.lastIndex&&feedback==AttemptState.CORRECT){Card(colors=CardDefaults.cardColors(containerColor=ElevatedSurface)){Column(Modifier.padding(20.dp)){Text("SESSION COMPLETE",color=Lime,style=MaterialTheme.typography.labelLarge);Text(accuracy.toString()+"% accuracy",style=MaterialTheme.typography.headlineSmall);Text(elapsed.toString()+" seconds · "+correct+" correct",color=TextSecondary)}}}
        else Button(onClick={if(ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){running=true;scope.launch{audio.start()}}else launcher.launch(Manifest.permission.RECORD_AUDIO)},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(14.dp)){Text(if(running)"LISTENING…" else "START LISTENING")}
    }
}