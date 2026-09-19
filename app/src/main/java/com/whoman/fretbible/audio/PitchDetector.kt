package com.whoman.fretbible.audio

import com.whoman.fretbible.core.model.DetectedNote
import com.whoman.fretbible.core.model.NoteName
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.round

class PitchDetector(
    private val sampleRate: Int = 44_100,
    private val minHz: Double = 65.0,
    private val maxHz: Double = 1_000.0
) {
    fun detect(samples: FloatArray): DetectedNote? {
        if (samples.size < 2048) return null
        if (rms(samples) < 0.008f) return null
        val frequency = autocorrelationFrequency(samples) ?: return null
        if (frequency !in minHz..maxHz) return null
        val midiFloat = 69.0 + 12.0 * log2(frequency / 440.0)
        val midi = round(midiFloat).toInt()
        val idealHz = 440.0 * 2.0.pow((midi - 69) / 12.0)
        val cents = 1200.0 * log2(frequency / idealHz)
        return DetectedNote(frequency, midi, NoteName.fromMidi(midi), (midi / 12) - 1, cents, confidence(samples, frequency))
    }

    private fun autocorrelationFrequency(x: FloatArray): Double? {
        val minLag = (sampleRate / maxHz).toInt()
        val maxLag = (sampleRate / minHz).toInt().coerceAtMost(x.size / 2)
        if (maxLag <= minLag) return null
        var bestLag = -1
        var bestScore = Double.NEGATIVE_INFINITY
        for (lag in minLag..maxLag) {
            var sum = 0.0; var a = 0.0; var b = 0.0
            for (i in 0 until x.size - lag) {
                val p = x[i].toDouble(); val q = x[i + lag].toDouble()
                sum += p*q; a += p*p; b += q*q
            }
            val score = sum / (sqrt(a*b) + 1e-12)
            if (score > bestScore) { bestScore = score; bestLag = lag }
        }
        if (bestLag < 0 || bestScore < 0.72) return null
        return sampleRate.toDouble() / bestLag
    }

    private fun confidence(x: FloatArray, frequency: Double): Double {
        val lag = (sampleRate / frequency).toInt().coerceAtLeast(1)
        if (lag >= x.size) return 0.0
        var sum = 0.0; var a = 0.0; var b = 0.0
        for (i in 0 until x.size - lag) {
            val p=x[i].toDouble(); val q=x[i+lag].toDouble()
            sum += p*q; a += p*p; b += q*q
        }
        return (sum / (sqrt(a*b) + 1e-12)).coerceIn(0.0, 1.0)
    }

    private fun rms(x: FloatArray): Float {
        var sum = 0.0
        for (v in x) sum += v*v
        return sqrt(sum / x.size).toFloat()
    }
}
