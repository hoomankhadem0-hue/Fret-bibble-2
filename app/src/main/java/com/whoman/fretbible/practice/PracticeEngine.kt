package com.whoman.fretbible.practice
import com.whoman.fretbible.core.model.*
import kotlin.random.Random
enum class AttemptState{LISTENING,CORRECT,TOO_HIGH,TOO_LOW,WRONG_NOTE}
data class FretStats(var attempts:Int=0,var correct:Int=0){val accuracy:Int get()=if(attempts==0)0 else correct*100/attempts}
class PracticeEngine{
 private val stats=mutableMapOf<Pair<Int,Int>,FretStats>()
 fun evaluate(target:TargetNote,detected:DetectedNote):AttemptState{if(detected.confidence<0.18)return AttemptState.LISTENING;if(detected.midi==target.midi)return AttemptState.CORRECT;return when{detected.midi==target.midi+1->AttemptState.TOO_HIGH;detected.midi==target.midi-1->AttemptState.TOO_LOW;else->AttemptState.WRONG_NOTE}}
 fun record(target:TargetNote,correct:Boolean){val s=stats.getOrPut(target.stringNumber to target.fret){FretStats()};s.attempts++;if(correct)s.correct++}
 fun score(target:TargetNote)=stats[target.stringNumber to target.fret]?:FretStats()
 fun newSession(count:Int=24,maxFret:Int=21,mode:PracticeMode=PracticeMode.ADAPTIVE):List<TargetNote>{val pool=FretboardData.all(maxFret.coerceIn(0,21)).map{it.toTarget()};if(pool.isEmpty())return emptyList();val safe=count.coerceIn(1,pool.size);return when(mode){PracticeMode.RANDOM->pool.shuffled(Random(System.nanoTime())).take(safe);PracticeMode.ADAPTIVE->adaptive(pool,safe);PracticeMode.PATTERN->pattern(pool,safe)}}
 private fun adaptive(pool:List<TargetNote>,count:Int):List<TargetNote>{val weighted=pool.flatMap{t->val s=score(t);val w=when{s.attempts==0->3;s.accuracy<50->8;s.accuracy<75->5;else->2};List(w){t}};return(weighted.shuffled(Random(System.nanoTime()))+pool.shuffled(Random(System.nanoTime()))).distinctBy{it.stringNumber to it.fret}.take(count)}
 private fun pattern(pool:List<TargetNote>,count:Int):List<TargetNote>{val seed=pool.sortedBy{score(it).accuracy}.take((pool.size/5).coerceAtLeast(8)).random();val sameString=pool.filter{it.stringNumber==seed.stringNumber}.sortedBy{kotlin.math.abs(it.fret-seed.fret)};val pattern=(sameString+pool.filter{it.note==seed.note}+pool.sortedBy{score(it).accuracy}).distinctBy{it.stringNumber to it.fret};return(pattern+pool.shuffled(Random(System.nanoTime()))).distinctBy{it.stringNumber to it.fret}.take(count)}
 private fun FretPosition.toTarget()=TargetNote(note,octave,stringNumber,fret)
}