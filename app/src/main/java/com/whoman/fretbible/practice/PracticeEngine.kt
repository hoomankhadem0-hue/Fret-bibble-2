package com.whoman.fretbible.practice

import com.whoman.fretbible.core.model.DetectedNote
import com.whoman.fretbible.core.model.NoteName
import com.whoman.fretbible.core.model.TargetNote
import kotlin.math.abs

enum class AttemptState { LISTENING, CORRECT, TOO_HIGH, TOO_LOW, WRONG_NOTE }
data class PracticeState(
    val targets: List<TargetNote>, val index: Int = 0, val attempts: Int = 0, val correct: Int = 0,
    val state: AttemptState = AttemptState.LISTENING, val detected: DetectedNote? = null, val elapsedSeconds: Int = 0
) {
    val current: TargetNote get() = targets[index.coerceIn(0, targets.lastIndex)]
    val complete: Boolean get() = index >= targets.size
    val accuracy: Int get() = if (attempts == 0) 0 else ((correct * 100.0) / attempts).toInt()
}
class PracticeEngine(private val toleranceCents: Double = 35.0) {
    fun evaluate(target: TargetNote, detected: DetectedNote): AttemptState {
        if (detected.note != target.note) return AttemptState.WRONG_NOTE
        if (abs(detected.cents) > toleranceCents) return if (detected.cents > 0) AttemptState.TOO_HIGH else AttemptState.TOO_LOW
        return AttemptState.CORRECT
    }
    fun targets() = listOf(
        TargetNote(NoteName.E), TargetNote(NoteName.A), TargetNote(NoteName.D), TargetNote(NoteName.G),
        TargetNote(NoteName.B), TargetNote(NoteName.E), TargetNote(NoteName.C), TargetNote(NoteName.F),
        TargetNote(NoteName.G), TargetNote(NoteName.D), TargetNote(NoteName.A), TargetNote(NoteName.E)
    )
}
