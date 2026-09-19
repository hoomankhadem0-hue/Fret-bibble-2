package com.whoman.fretbible.core.model

data class FretPosition(
    val stringNumber: Int,
    val fret: Int,
    val midi: Int,
    val note: NoteName,
    val octave: Int
) {
    val label: String get() = note.display + octave
}

object FretboardData {
    private val openMidi = intArrayOf(64, 59, 55, 50, 45, 40)

    fun all(maxFret: Int = 24): List<FretPosition> =
        (1..6).flatMap { stringNumber ->
            (0..maxFret).map { fret ->
                val midi = openMidi[stringNumber - 1] + fret
                FretPosition(stringNumber, fret, midi, NoteName.fromMidi(midi), (midi / 12) - 1)
            }
        }

    fun forString(stringNumber: Int, maxFret: Int = 24): List<FretPosition> =
        all(maxFret).filter { it.stringNumber == stringNumber }
}
