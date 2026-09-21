package com.whoman.fretbible.analyzer

import kotlin.math.*

data class AnalyzerResult(
    val bpm: Double,
    val bpmConfidence: Double,
    val key: String,
    val keyConfidence: Double,
    val chords: List<ChordEstimate>
)

data class ChordEstimate(
    val symbol: String,
    val startSeconds: Double,
    val confidence: Double
)

/**
 * Lightweight, dependency-free music analysis primitives.
 *
 * The BPM estimator works on an RMS/onset-like energy envelope.
 * Key/chord estimation uses chroma templates once chroma frames are supplied.
 * It intentionally exposes confidence rather than pretending uncertain
 * full-mix transcription is exact.
 */
object MusicAnalyzer {
    fun estimateBpm(envelope: FloatArray, sampleRate: Int, hopSize: Int): Pair<Double, Double> {
        if (envelope.size < 8) return 0.0 to 0.0
        val diff = FloatArray(envelope.size) { i -> if (i == 0) 0f else max(0f, envelope[i] - envelope[i - 1]) }
        var bestBpm = 0.0
        var bestScore = Double.NEGATIVE_INFINITY
        val minLag = floor((60.0 / 180.0) * sampleRate / hopSize).toInt().coerceAtLeast(1)
        val maxLag = ceil((60.0 / 60.0) * sampleRate / hopSize).toInt().coerceAtMost(diff.lastIndex)
        for (lag in minLag..maxLag) {
            var score = 0.0
            var count = 0
            var i = lag
            while (i < diff.size) { score += diff[i].toDouble() * diff[i - lag]; count++; i++ }
            if (count > 0) {
                val normalized = score / count
                if (normalized > bestScore) {
                    bestScore = normalized
                    bestBpm = 60.0 * sampleRate / (hopSize * lag)
                }
            }
        }
        var bpm = bestBpm
        while (bpm < 80) bpm *= 2.0
        while (bpm > 160) bpm /= 2.0
        return bpm to if (bestScore.isFinite()) (bestScore / (bestScore + 1.0)).coerceIn(0.0, 1.0) else 0.0
    }

    fun estimateKey(chroma: DoubleArray): Pair<String, Double> {
        if (chroma.size != 12) return "Unknown" to 0.0
        val major = doubleArrayOf(6.35,2.23,3.48,2.33,4.38,4.09,2.52,5.19,2.39,3.66,2.29,2.88)
        val minor = doubleArrayOf(6.33,2.68,3.52,5.38,2.60,3.53,2.54,4.75,3.98,2.69,3.34,3.17)
        val names = arrayOf("C","C#","D","Eb","E","F","F#","G","Ab","A","Bb","B")
        var best = Double.NEGATIVE_INFINITY
        var second = Double.NEGATIVE_INFINITY
        var bestName = "Unknown"
        for (root in 0..11) for (mode in 0..1) {
            val profile = if (mode == 0) major else minor
            var dot = 0.0
            var norm = 0.0
            for (i in 0..11) { dot += chroma[i] * profile[(i-root+12)%12]; norm += chroma[i]*chroma[i] }
            val score = if (norm > 1e-12) dot / sqrt(norm * profile.sumOf { it*it }) else 0.0
            if (score > best) { second = best; best = score; bestName = names[root] + if (mode == 0) " Major" else " Minor" }
            else if (score > second) second = score
        }
        val confidence = ((best-second) / (1.0-(-1.0))).coerceIn(0.0,1.0)
        return bestName to confidence
    }
}
