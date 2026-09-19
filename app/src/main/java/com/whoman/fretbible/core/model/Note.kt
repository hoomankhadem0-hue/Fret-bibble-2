package com.whoman.fretbible.core.model

enum class NoteName(val chromaticIndex: Int) {
    C(0), CS(1), D(2), DS(3), E(4), F(5), FS(6), G(7), GS(8), A(9), AS(10), B(11);
    val display: String get() = when (this) {
        CS -> "C#"; DS -> "D#"; FS -> "F#"; GS -> "G#"; AS -> "A#"; else -> name
    }
    companion object { fun fromMidi(midi: Int): NoteName = entries[midi.mod(12)] }
}
data class DetectedNote(val frequencyHz: Double, val midi: Int, val note: NoteName, val octave: Int, val cents: Double, val confidence: Double)
data class TargetNote(val note: NoteName, val octave: Int? = null, val stringNumber: Int? = null)
