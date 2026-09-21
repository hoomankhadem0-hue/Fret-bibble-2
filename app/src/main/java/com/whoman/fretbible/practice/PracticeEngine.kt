package com.whoman.fretbible.practice

import com.whoman.fretbible.core.model.*
import kotlin.random.Random

enum class AttemptState { LISTENING, CORRECT, TOO_HIGH, TOO_LOW, WRONG_NOTE }

data class FretStats(var attempts: Int = 0, var correct: Int = 0) {
    val accuracy: Int get() = if (attempts == 0) 0 else correct * 100 / attempts
}

class PracticeEngine {
    private val stats = mutableMapOf<Pair<Int, Int>, FretStats>()

    fun evaluate(target: TargetNote, detected: DetectedNote): AttemptState {
        if (detected.confidence < 0.18) return AttemptState.LISTENING
        if (detected.midi == target.midi) return AttemptState.CORRECT

        return when {
            detected.midi == target.midi + 1 -> AttemptState.TOO_HIGH
            detected.midi == target.midi - 1 -> AttemptState.TOO_LOW
            else -> AttemptState.WRONG_NOTE
        }
    }

    fun record(target: TargetNote, correct: Boolean) {
        val key = target.stringNumber to target.fret
        val s = stats.getOrPut(key) { FretStats() }
        s.attempts++
        if (correct) s.correct++
    }

    fun score(target: TargetNote): FretStats =
        stats[target.stringNumber to target.fret] ?: FretStats()

    /** A new session samples the complete 150-position standard-tuning neck. */
    fun newSession(count: Int = 24): List<TargetNote> {
        return FretboardData.all(24)
            .shuffled(Random(System.nanoTime()))
            .take(count.coerceAtMost(150))
            .map { it.toTarget() }
    }

    private fun FretPosition.toTarget() =
        TargetNote(note, octave, stringNumber, fret)

    fun targets() = listOf(
        TargetNote(NoteName.B, 3, 2, 0),
        TargetNote(NoteName.C, 4, 2, 1),
        TargetNote(NoteName.AS, 3, 2, 11),
        TargetNote(NoteName.D, 4, 3, 0),
        TargetNote(NoteName.F, 4, 4, 3),
        TargetNote(NoteName.G, 4, 1, 3),
        TargetNote(NoteName.A, 2, 5, 0),
        TargetNote(NoteName.E, 2, 6, 0),
        TargetNote(NoteName.G, 3, 6, 3),
        TargetNote(NoteName.D, 4, 2, 3),
        TargetNote(NoteName.FS, 3, 2, 7),
        TargetNote(NoteName.C, 4, 1, 8)
    )
}
