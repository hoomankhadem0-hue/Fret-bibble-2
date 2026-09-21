package com.whoman.fretbible.analyzer

import kotlin.math.*

data class AnalyzerResult(
    val bpm: Double,
    val bpmConfidence: Double,
    val key: String,
    val keyConfidence: Double,
    val chords: List<ChordEstimate>,
    val progressionMatches: List<ProgressionMatch> = emptyList()
)

data class ProgressionMatch(
    val pattern: ProgressionPattern,
    val startIndex: Int,
    val length: Int,
    val score: Double
)

data class ChordEstimate(
    val symbol: String,
    val startSeconds: Double,
    val confidence: Double
)

object MusicAnalyzer {
    private val noteNames = arrayOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")

    private val chordTemplates = listOf(
        "maj" to intArrayOf(0, 4, 7),
        "min" to intArrayOf(0, 3, 7),
        "dim" to intArrayOf(0, 3, 6),
        "7" to intArrayOf(0, 4, 7, 10),
        "maj7" to intArrayOf(0, 4, 7, 11),
        "m7" to intArrayOf(0, 3, 7, 10),
        "sus2" to intArrayOf(0, 2, 7),
        "sus4" to intArrayOf(0, 5, 7),
        "5" to intArrayOf(0, 7)
    )

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
            while (i < diff.size) {
                score += diff[i].toDouble() * diff[i - lag]
                count++
                i++
            }
            if (count > 0) {
                val normalized = score / count
                if (normalized > bestScore) {
                    bestScore = normalized
                    bestBpm = 60.0 * sampleRate / (hopSize * lag)
                }
            }
        }
        var bpm = bestBpm
        while (bpm in 0.1..79.999) bpm *= 2.0
        while (bpm > 160.0) bpm /= 2.0
        return bpm to if (bestScore.isFinite()) (bestScore / (bestScore + 1.0)).coerceIn(0.0, 1.0) else 0.0
    }

    fun estimateKey(chroma: DoubleArray): Pair<String, Double> {
        if (chroma.size != 12) return "Unknown" to 0.0
        val major = doubleArrayOf(6.35,2.23,3.48,2.33,4.38,4.09,2.52,5.19,2.39,3.66,2.29,2.88)
        val minor = doubleArrayOf(6.33,2.68,3.52,5.38,2.60,3.53,2.54,4.75,3.98,2.69,3.34,3.17)
        var best = Double.NEGATIVE_INFINITY
        var second = Double.NEGATIVE_INFINITY
        var bestName = "Unknown"
        for (root in 0..11) for (mode in 0..1) {
            val profile = if (mode == 0) major else minor
            var dot = 0.0
            var norm = 0.0
            for (i in 0..11) {
                dot += chroma[i] * profile[(i - root + 12) % 12]
                norm += chroma[i] * chroma[i]
            }
            val score = if (norm > 1e-12) dot / sqrt(norm * profile.sumOf { it * it }) else 0.0
            if (score > best) {
                second = best
                best = score
                bestName = noteNames[root] + if (mode == 0) " Major" else " Minor"
            } else if (score > second) second = score
        }
        val confidence = ((best - second) * 4.0).coerceIn(0.0, 1.0)
        return bestName to confidence
    }

    fun estimateChords(
        chromaFrames: Array<DoubleArray>,
        sampleRate: Int,
        hopSize: Int,
        minChangeSeconds: Double = 0.75,
        key: String? = null
    ): List<ChordEstimate> {
        if (chromaFrames.isEmpty()) return emptyList()
        val raw = ArrayList<ChordEstimate>()
        var lastSymbol: String? = null
        var lastStart = 0.0
        var lastConfidence = 0.0

        for (index in chromaFrames.indices) {
            val frame = chromaFrames[index]
            val (symbol, confidence) = bestChord(frame, lastSymbol, key)
            val time = index * hopSize.toDouble() / sampleRate
            if (symbol != lastSymbol && (lastSymbol == null || time - lastStart >= minChangeSeconds)) {
                if (lastSymbol != null) raw += ChordEstimate(lastSymbol!!, lastStart, lastConfidence)
                lastSymbol = symbol
                lastStart = time
                lastConfidence = confidence
            } else if (symbol == lastSymbol) {
                lastConfidence = (lastConfidence * 0.7 + confidence * 0.3)
            }
        }
        if (lastSymbol != null) raw += ChordEstimate(lastSymbol!!, lastStart, lastConfidence)
        return mergeShortChanges(raw, minChangeSeconds)
    }

    private fun bestChord(chroma: DoubleArray, previousSymbol: String? = null, key: String? = null): Pair<String, Double> {
        if (chroma.size != 12) return "—" to 0.0
        val energy = sqrt(chroma.sumOf { it * it })
        if (energy < 1e-6) return "—" to 0.0
        var best = Double.NEGATIVE_INFINITY
        var second = Double.NEGATIVE_INFINITY
        var bestSymbol = "—"
        for (root in 0..11) {
            for ((suffix, intervals) in chordTemplates) {
                var score = 0.0
                for (i in intervals) score += chroma[(root + i) % 12]
                score /= intervals.size
                val penalty = (chroma.sum() - intervals.sumOf { chroma[(root + it) % 12] }) * 0.08
                score -= penalty
                if (key != null) {
                    val roman = ProgressionLibrary.romanFor(noteNames[root] + when (suffix) {
                        "maj" -> ""
                        "min" -> "m"
                        else -> suffix
                    }, key)
                    if (roman != null) score += 0.035
                }
                if (previousSymbol != null) {
                    val romanCandidate = ProgressionLibrary.romanFor(noteNames[root] + when (suffix) {
                        "maj" -> ""
                        "min" -> "m"
                        else -> suffix
                    }, key ?: "")
                    val romanPrevious = ProgressionLibrary.romanFor(previousSymbol, key ?: "")
                    if (romanCandidate != null && romanPrevious != null) {
                        score += ProgressionLibrary.transitionPrior(romanPrevious, romanCandidate) * 0.22
                    }
                }
                if (score > best) {
                    second = best
                    best = score
                    bestSymbol = noteNames[root] + when (suffix) {
                        "maj" -> ""
                        "min" -> "m"
                        else -> suffix
                    }
                } else if (score > second) second = score
            }
        }
        return bestSymbol to ((best - second) * 3.0).coerceIn(0.0, 1.0)
    }

    fun matchProgressions(chords: List<ChordEstimate>, key: String, maxResults: Int = 5): List<ProgressionMatch> {
        if (chords.size < 2 || key == "Unknown") return emptyList()

        val romanChords = chords.mapIndexedNotNull { index, chord ->
            ProgressionLibrary.romanFor(chord.symbol, key)?.let { index to (it to chord) }
        }
        if (romanChords.size < 2) return emptyList()

        val matches = mutableListOf<ProgressionMatch>()
        for (pattern in ProgressionLibrary.patterns) {
            val p = pattern.numerals
            if (p.size < 2) continue

            for (startIndex in romanChords.indices) {
                val maxLen = minOf(p.size, romanChords.size - startIndex)
                if (maxLen < 2) continue

                for (len in maxLen downTo 2) {
                    var exact = 0.0
                    var weighted = 0.0
                    for (i in 0 until len) {
                        if (baseRoman(romanChords[startIndex + i].second.first) == baseRoman(p[i])) {
                            exact += 1.0
                            weighted += if (romanChords[startIndex + i].second.second.confidence >= 0.65) 1.0 else 0.8
                        }
                    }

                    val fit = weighted / len
                    val coverage = len.toDouble() / p.size.coerceAtLeast(1)
                    val lengthBonus = (len - 1).toDouble() / (p.size.coerceAtLeast(2) - 1)
                    val score = (fit * 0.62 + coverage * 0.18 + lengthBonus * 0.20) *
                        pattern.weight.coerceAtMost(1.5) / 1.5

                    if (score >= 0.66 && (exact >= len - 1 || len <= 2)) {
                        matches += ProgressionMatch(pattern, romanChords[startIndex].first, len, score.coerceIn(0.0, 1.0))
                    }
                }
            }
        }

        return matches
            .sortedWith(compareByDescending<ProgressionMatch> { it.score }.thenByDescending { it.length })
            .filter { candidate ->
                matches.none { other ->
                    other !== candidate &&
                    other.pattern.name == candidate.pattern.name &&
                    abs(other.startIndex - candidate.startIndex) <= 1 &&
                    other.length > candidate.length &&
                    other.score >= candidate.score - 0.03
                }
            }
            .take(maxResults)
    }

    private fun baseRoman(value: String): String =
        value.replace("♭", "b").replace("°", "").replace("ø", "")
            .replace(Regex("(maj7|m7|7|6|sus2|sus4)$"), "")

    private fun mergeShortChanges(input: List<ChordEstimate>, minDuration: Double): List<ChordEstimate> {
        if (input.size < 2) return input
        val result = input.toMutableList()
        var i = 0
        while (i < result.lastIndex) {
            val duration = result[i + 1].startSeconds - result[i].startSeconds
            if (duration < minDuration) {
                result.removeAt(i)
                if (i > 0) i--
            } else {
                i++
            }
        }
        return result
    }
}
