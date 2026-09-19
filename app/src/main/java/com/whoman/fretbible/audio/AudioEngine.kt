package com.whoman.fretbible.audio
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.whoman.fretbible.core.model.DetectedNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
class AudioEngine{
companion object{const val SAMPLE_RATE=44100}
private val detector=PitchDetector(SAMPLE_RATE);private val running=AtomicBoolean(false);private var recorder:AudioRecord?=null
private val _detected=MutableStateFlow<DetectedNote?>(null);val detected: StateFlow<DetectedNote?> = _detected
private val _level=MutableStateFlow(0f);val level: StateFlow<Float> = _level
suspend fun start(){
if(running.getAndSet(true))return
val minBuffer=AudioRecord.getMinBufferSize(SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT)
if(minBuffer<=0){running.set(false);return}
val bufferSize=(minBuffer*3).coerceAtLeast(16384)
recorder=try{AudioRecord(MediaRecorder.AudioSource.UNPROCESSED,SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,bufferSize)}catch(_:Throwable){AudioRecord(MediaRecorder.AudioSource.MIC,SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,bufferSize)}
withContext(Dispatchers.IO){val local=recorder?:return@withContext
try{local.startRecording();val buffer=ShortArray(8192);while(running.get()){val count=local.read(buffer,0,buffer.size,AudioRecord.READ_BLOCKING);if(!running.get())break;if(count==buffer.size){val samples=FloatArray(count){i->buffer[i]/32768f};var sum=0.0;for(v in samples)sum+=v*v;_level.value=kotlin.math.sqrt(sum/count).toFloat();_detected.value=detector.detect(samples)}}}catch(_:Throwable){}finally{try{local.stop()}catch(_:Throwable){}}}}
fun stop(){running.set(false);val local=recorder;recorder=null;try{local?.stop()}catch(_:Throwable){};try{local?.release()}catch(_:Throwable){};_detected.value=null;_level.value=0f}}
