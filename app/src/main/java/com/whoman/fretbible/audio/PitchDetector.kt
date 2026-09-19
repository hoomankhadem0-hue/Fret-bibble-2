package com.whoman.fretbible.audio

import com.whoman.fretbible.core.model.*
import kotlin.math.*

/**
 * Guitar-focused monophonic YIN pitch detector.
 *
 * Important: CMNDF is accumulated from lag 1, not from minLag. Starting the
 * cumulative sum at minLag biases the curve and can make otherwise correct
 * notes jump to a neighboring harmonic.
 */
class PitchDetector(
    private val sampleRate: Int = 44100,
    private val minHz: Double = 70.0,
    private val maxHz: Double = 750.0
) {
    fun detect(samples: FloatArray): DetectedNote? {
        if (samples.size < 4096) return null

        val rawRms = rms(samples)
        if (rawRms < AudioSettings.sensitivity) return null

        val x = samples.copyOf()
        removeDc(x)
        applyHann(x)

        val result = yin(x) ?: return null
        val frequency = result.first
        val confidence = result.second

        // YIN already rejected the frame if it cannot find a periodic candidate.
        // Keep this threshold deliberately permissive; the practice screen adds
        // temporal stability before accepting a note.
        if (frequency !in minHz..maxHz || confidence < 0.18) return null

        val midiFloat = 69.0 + 12.0 * log2(frequency / 440.0)
        val midi = round(midiFloat).toInt()
        val idealHz = 440.0 * 2.0.pow((midi - 69) / 12.0)
        val cents = 1200.0 * log2(frequency / idealHz)

        return DetectedNote(
            frequencyHz = frequency,
            midi = midi,
            note = NoteName.fromMidi(midi),
            octave = (midi / 12) - 1,
            cents = cents,
            confidence = confidence
        )
    }

    private fun yin(x: FloatArray): Pair<Double, Double>? {
        val minLag = ceil(sampleRate / maxHz).toInt().coerceAtLeast(2)
        val maxLag = floor(sampleRate / minHz).toInt().coerceAtMost(x.size / 2 - 1)
        if (minLag >= maxLag) return null

        val difference = DoubleArray(maxLag + 1)

        // Difference function.
        for (lag in 1..maxLag) {
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

        // Correct CMNDF: denominator is sum(d(1)..d(tau)).
        var running = 0.0
        val cmndf = DoubleArray(maxLag + 1)
        cmndf[0] = 1.0

        for (lag in 1..maxLag) {
            running += difference[lag]
            cmndf[lag] = if (running <= 1e-12) {
                1.0
            } else {
                difference[lag] * lag / running
            }
        }

        // YIN absolute threshold: take the first dip below threshold,
        // then descend to its local minimum.
        val threshold = 0.16
        var bestLag = -1
        for (lag in minLag..maxLag) {
            if (cmndf[lag] < threshold) {
                var candidate = lag
                while (candidate + 1 <= maxLag && cmndf[candidate + 1] < cmndf[candidate]) {
                    candidate++
                }
                bestLag = candidate
                break
            }
        }

        // If no threshold crossing exists, use the best candidate in the guitar range.
        if (bestLag < 0) {
            var best = Double.POSITIVE_INFINITY
            for (lag in minLag..maxLag) {
                if (cmndf[lag] < best) {
                    best = cmndf[lag]
                    bestLag = lag
                }
            }
        }

        if (bestLag < 0) return null

        val bestValue = cmndf[bestLag]
        val refinedLag = if (bestLag > 1 && bestLag < maxLag) {
            val y1 = cmndf[bestLag - 1]
            val y2 = cmndf[bestLag]
            val y3 = cmndf[bestLag + 1]
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
