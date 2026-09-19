package com.whoman.fretbible.practice
import com.whoman.fretbible.core.model.*
import kotlin.math.abs
enum class AttemptState{LISTENING,CORRECT,TOO_HIGH,TOO_LOW,WRONG_NOTE}
class PracticeEngine(private val toleranceCents:Double=35.0){
fun evaluate(target:TargetNote,detected:DetectedNote):AttemptState{
if(detected.confidence<0.78)return AttemptState.LISTENING
if(detected.midi!=target.midi)return AttemptState.WRONG_NOTE
if(abs(detected.cents)>toleranceCents)return if(detected.cents>0)AttemptState.TOO_HIGH else AttemptState.TOO_LOW
return AttemptState.CORRECT}
fun targets()=listOf(
TargetNote(NoteName.B,3,2,0),TargetNote(NoteName.C,4,2,1),TargetNote(NoteName.AS,3,2,11),
TargetNote(NoteName.D,4,3,0),TargetNote(NoteName.F,4,4,3),TargetNote(NoteName.G,4,1,3),
TargetNote(NoteName.A,2,5,0),TargetNote(NoteName.E,2,6,0),TargetNote(NoteName.G,3,6,3),
TargetNote(NoteName.D,4,2,3),TargetNote(NoteName.FS,3,2,7),TargetNote(NoteName.C,4,1,8))}
