package com.whoman.fretbible.practice

import com.whoman.fretbible.core.model.*

enum class AttemptState { LISTENING, CORRECT, TOO_HIGH, TOO_LOW, WRONG_NOTE }

data class FretStats(var attempts: Int = 0, var correct: Int = 0) {
    val accuracy: Int get() = if (attempts == 0) 0 else correct * 100 / attempts
}

class PracticeEngine {
    private val stats = mutableMapOf<Pair<Int, Int>, FretStats>()

    fun evaluate(target: TargetNote, detected: DetectedNote): AttemptState {
        // The exercise verifies the musical note, not tuning precision.
        // B3 at 244 Hz, 247 Hz, etc. is still B3 for fretboard training.
        if (detected.confidence < 0.18) return AttemptState.LISTENING

        if (detected.midi == target.midi) {
            return AttemptState.CORRECT
        }

        // Keep useful tuning feedback when the detector is exactly one
        // semitone away from the requested note.
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

    fun nextTargets(count: Int = 12): List<TargetNote> {
        val pool = targets()
        return pool.sortedBy { score(it).accuracy }.take((count / 2).coerceAtLeast(1)) +
            pool.shuffled().take((count + 1) / 2)
    }

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
