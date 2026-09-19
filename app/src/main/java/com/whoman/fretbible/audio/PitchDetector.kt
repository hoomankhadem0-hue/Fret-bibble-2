package com.whoman.fretbible.audio
import com.whoman.fretbible.core.model.*
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt
class PitchDetector(private val sampleRate:Int=44100,private val minHz:Double=65.0,private val maxHz:Double=1000.0){
fun detect(samples:FloatArray):DetectedNote?{if(samples.size<4096)return null
val x=samples.copyOf();removeDc(x);applyHann(x);if(rms(x)<0.006f)return null
val result=autocorrelationFrequency(x)?:return null;val frequency=result.first;val confidence=result.second
if(frequency !in minHz..maxHz||confidence<0.74)return null
val midiFloat=69.0+12.0*log2(frequency/440.0);val midi=round(midiFloat).toInt()
val idealHz=440.0*2.0.pow((midi-69)/12.0);val cents=1200.0*log2(frequency/idealHz)
return DetectedNote(frequency,midi,NoteName.fromMidi(midi),(midi/12)-1,cents,confidence)}
private fun autocorrelationFrequency(x:FloatArray):Pair<Double,Double>?{
val minLag=(sampleRate/maxHz).toInt().coerceAtLeast(2);val maxLag=(sampleRate/minHz).toInt().coerceAtMost(x.size/2);if(maxLag<=minLag)return null
var bestLag=-1;var bestScore=Double.NEGATIVE_INFINITY
for(lag in minLag..maxLag){var sum=0.0;var a=0.0;var b=0.0;for(i in 0 until x.size-lag){val p=x[i].toDouble();val q=x[i+lag].toDouble();sum+=p*q;a+=p*p;b+=q*q};val score=sum/(sqrt(a*b)+1e-12);if(score>bestScore){bestScore=score;bestLag=lag}}
if(bestLag<0||bestScore<0.74)return null
return (sampleRate.toDouble()/bestLag) to bestScore.coerceIn(0.0,1.0)}
private fun removeDc(x:FloatArray){var mean=0.0;for(v in x)mean+=v;mean/=x.size;for(i in x.indices)x[i]=(x[i]-mean).toFloat()}
private fun applyHann(x:FloatArray){val n=x.lastIndex.coerceAtLeast(1);for(i in x.indices){val w=.5-.5*kotlin.math.cos(2.0*Math.PI*i/n);x[i]=(x[i]*w).toFloat()}}
private fun rms(x:FloatArray):Float{var sum=0.0;for(v in x)sum+=v*v;return sqrt(sum/x.size).toFloat()}}
