package com.whoman.fretbible.core.model
enum class NoteName(val chromaticIndex:Int){C(0),CS(1),D(2),DS(3),E(4),F(5),FS(6),G(7),GS(8),A(9),AS(10),B(11);
val display:String get()=when(this){CS->"Db";DS->"Eb";FS->"Gb";GS->"Ab";AS->"Bb";else->name}
companion object{fun fromMidi(midi:Int)=entries[midi.mod(12)]}}
data class DetectedNote(val frequencyHz:Double,val midi:Int,val note:NoteName,val octave:Int,val cents:Double,val confidence:Double)
data class TargetNote(val note:NoteName,val octave:Int,val stringNumber:Int,val fret:Int){
val midi:Int get()=(octave+1)*12+note.chromaticIndex
val label:String get()="String ${stringNumber} · ${note.display}${octave}"
val instruction:String get()="Play the ${when(stringNumber){1->"1st";2->"2nd";3->"3rd";else->"${stringNumber}th"}} string · ${note.display}${octave}"
}