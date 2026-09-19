package com.whoman.fretbible.audio

import com.whoman.fretbible.core.model.*
import kotlin.math.*

/**
 * Monophonic guitar pitch detector using a YIN-style difference function.
 * This is less likely than raw autocorrelation to lock onto a strong harmonic.
 */
class PitchDetector(
    private val sampleRate: Int = 44100,
    private val minHz: Double = 70.0,
    private val maxHz: Double = 750.0
) {
    fun detect(samples: FloatArray): DetectedNote? {
        if (samples.size < 4096) return null
        if (rms(samples) < AudioSettings.sensitivity) return null

        val x = samples.copyOf()
        removeDc(x)
        applyHann(x)

        val result = yin(x) ?: return null
        val frequency = result.first
        val confidence = result.second
        if (frequency !in minHz..maxHz || confidence < 0.35) return null

        val midiFloat = 69.0 + 12.0 * log2(frequency / 440.0)
        val midi = round(midiFloat).toInt()
        val idealHz = 440.0 * 2.0.pow((midi - 69) / 12.0)
        val cents = 1200.0 * log2(frequency / idealHz)

        return DetectedNote(
            frequency,
            midi,
            NoteName.fromMidi(midi),
            (midi / 12) - 1,
            cents,
            confidence
        )
    }

    private fun yin(x: FloatArray): Pair<Double, Double>? {
        val minLag = ceil(sampleRate / maxHz).toInt().coerceAtLeast(2)
        val maxLag = floor(sampleRate / minHz).toInt().coerceAtMost(x.size / 2 - 1)
        if (minLag >= maxLag) return null

        val difference = DoubleArray(maxLag + 1)

        for (lag in minLag..maxLag) {
            var sum = 0.0
            var i = 0
            val limit = x.size - lag
            while (i < limit) {
                val d = x[i].toDouble() - x[i + lag].toDouble()
                sum += d * d
                i++
            }
            difference[lag] = sum
        }

        var running = 0.0
        var bestLag = -1
        var bestValue = Double.POSITIVE_INFINITY

        for (lag in minLag..maxLag) {
            running += difference[lag]
            val cmnd = if (running <= 1e-12) 1.0 else difference[lag] * lag / running

            if (cmnd < 0.20) {
                var localLag = lag
                var localValue = cmnd
                while (localLag + 1 <= maxLag) {
                    val next = difference[localLag + 1] * (localLag + 1) /
                        (running + difference[localLag + 1])
                    if (next > localValue) break
                    localLag++
                    localValue = next
                }
                bestLag = localLag
                bestValue = localValue
                break
            }

            if (cmnd < bestValue) {
                bestValue = cmnd
                bestLag = lag
            }
        }

        if (bestLag < 0 || !bestValue.isFinite()) return null

        val refinedLag = if (bestLag > minLag && bestLag < maxLag) {
            val y1 = difference[bestLag - 1]
            val y2 = difference[bestLag]
            val y3 = difference[bestLag + 1]
            val denominator = y1 - 2.0 * y2 + y3
            if (abs(denominator) > 1e-12) {
                bestLag + 0.5 * (y1 - y3) / denominator
            } else {
                bestLag.toDouble()
            }
        } else {
            bestLag.toDouble()
        }

        val frequency = sampleRate / refinedLag.coerceAtLeast(1.0)
        val confidence = (1.0 - bestValue).coerceIn(0.0, 1.0)
        return frequency to confidence
    }

    private fun removeDc(x: FloatArray) {
        var mean = 0.0
        for (v in x) mean += v
        mean /= x.size
        for (i in x.indices) x[i] = (x[i] - mean).toFloat()
    }

    private fun applyHann(x: FloatArray) {
        val n = x.lastIndex.coerceAtLeast(1)
        for (i in x.indices) {
            x[i] = (x[i] * (.5 - .5 * cos(2.0 * Math.PI * i / n))).toFloat()
        }
    }

    private fun rms(x: FloatArray): Float {
        var sum = 0.0
        for (v in x) sum += v.toDouble() * v.toDouble()
        return sqrt(sum / x.size).toFloat()
    }
}
