package com.whoman.fretbible.audio

import com.whoman.fretbible.core.model.*
import kotlin.math.*

class PitchDetector(
    private val sampleRate: Int = 44100,
    private val minHz: Double = 70.0,
    private val maxHz: Double = 700.0
) {
    fun detect(samples: FloatArray): DetectedNote? {
        if (samples.size < 4096) return null
        val x = samples.copyOf()
        removeDc(x)
        applyHann(x)
        if (rms(x) < AudioSettings.sensitivity) return null
        val result = yinLikeAutocorrelation(x) ?: return null
        val frequency = result.first
        val confidence = result.second
        if (frequency !in minHz..maxHz || confidence < 0.55) return null

        // Prefer a guitar fundamental over a strong octave harmonic.
        val corrected = correctOctave(frequency, x)
        val midiFloat = 69.0 + 12.0 * log2(corrected / 440.0)
        val midi = round(midiFloat).toInt()
        val idealHz = 440.0 * 2.0.pow((midi - 69) / 12.0)
        val cents = 1200.0 * log2(corrected / idealHz)
        return DetectedNote(corrected, midi, NoteName.fromMidi(midi), (midi / 12) - 1, cents, confidence)
    }

    private fun yinLikeAutocorrelation(x: FloatArray): Pair<Double, Double>? {
        val minLag = (sampleRate / maxHz).toInt().coerceAtLeast(2)
        val maxLag = (sampleRate / minHz).toInt().coerceAtMost(x.size / 2)
        var bestLag = -1
        var bestScore = Double.NEGATIVE_INFINITY
        var second = Double.NEGATIVE_INFINITY
        for (lag in minLag..maxLag) {
            var sum = 0.0
            var a = 0.0
            var b = 0.0
            for (i in 0 until x.size - lag) {
                val p = x[i].toDouble()
                val q = x[i + lag].toDouble()
                sum += p * q
                a += p * p
                b += q * q
            }
            val score = sum / (sqrt(a * b) + 1e-12)
            if (score > bestScore) {
                second = bestScore
                bestScore = score
                bestLag = lag
            } else if (score > second) second = score
        }
        if (bestLag < 0) return null
        val clarity = (bestScore - second).coerceAtLeast(0.0)
        return (sampleRate.toDouble() / bestLag) to (0.70 * bestScore + 0.30 * min(1.0, clarity * 8.0))
    }

    private fun correctOctave(frequency: Double, x: FloatArray): Double {
        val candidates = doubleArrayOf(frequency / 2.0, frequency, frequency * 2.0)
        return candidates
            .filter { it in minHz..maxHz }
            .maxByOrNull { periodicityAt(x, sampleRate / it) * if (it <= frequency) 1.06 else 1.0 } ?: frequency
    }

    private fun periodicityAt(x: FloatArray, lagDouble: Double): Double {
        val lag = lagDouble.roundToInt().coerceIn(2, x.size / 2)
        var sum = 0.0
        var a = 0.0
        var b = 0.0
        for (i in 0 until x.size - lag) {
            val p = x[i].toDouble()
            val q = x[i + lag].toDouble()
            sum += p * q
            a += p * p
            b += q * q
        }
        return sum / (sqrt(a * b) + 1e-12)
    }

    private fun removeDc(x: FloatArray) {
        var mean = 0.0
        for (v in x) mean += v
        mean /= x.size
        for (i in x.indices) x[i] = (x[i] - mean).toFloat()
    }

    private fun applyHann(x: FloatArray) {
        val n = x.lastIndex.coerceAtLeast(1)
        for (i in x.indices) x[i] = (x[i] * (.5 - .5 * cos(2.0 * Math.PI * i / n))).toFloat()
    }

    private fun rms(x: FloatArray): Float {
        var sum = 0.0
        for (v in x) sum += v * v
        return sqrt(sum / x.size).toFloat()
    }
}
