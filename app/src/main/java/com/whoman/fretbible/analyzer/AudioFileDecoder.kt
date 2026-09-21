package com.whoman.fretbible.analyzer

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.nio.ByteOrder
import kotlin.math.*

data class DecodedAudio(
    val samples: FloatArray,
    val sampleRate: Int,
    val channels: Int,
    val durationSeconds: Double
)

object AudioFileDecoder {
    fun decode(context: Context, uri: Uri, maxSeconds: Double = 180.0): DecodedAudio {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)
        var track = -1
        for (i in 0 until extractor.trackCount) {
            val f = extractor.getTrackFormat(i)
            if ((f.getString(MediaFormat.KEY_MIME) ?: "").startsWith("audio/")) { track = i; break }
        }
        require(track >= 0) { "No audio track found" }
        extractor.selectTrack(track)
        val format = extractor.getTrackFormat(track)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: error("Unsupported audio")
        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val encoding = if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
            format.getInteger(MediaFormat.KEY_PCM_ENCODING)
        } else {
            android.media.AudioFormat.ENCODING_PCM_16BIT
        }
        require(encoding == android.media.AudioFormat.ENCODING_PCM_16BIT) {
            "Only 16-bit PCM decoder output is supported"
        }

        val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) {
            format.getLong(MediaFormat.KEY_DURATION)
        } else 0L
        val maxSamples = (sampleRate * maxSeconds).toInt()
        val output = FloatArray(maxSamples)
        var write = 0
        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()
        val info = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false

        try {
            while (!outputDone && write < maxSamples) {
                if (!inputDone) {
                    val inputIndex = codec.dequeueInputBuffer(10_000)
                    if (inputIndex >= 0) {
                        val input = codec.getInputBuffer(inputIndex)
                        if (input != null) {
                            val size = extractor.readSampleData(input, 0)
                            if (size < 0) {
                                codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                inputDone = true
                            } else {
                                codec.queueInputBuffer(inputIndex, 0, size, extractor.sampleTime, 0)
                                extractor.advance()
                            }
                        }
                    }
                }

                when (val outputIndex = codec.dequeueOutputBuffer(info, 10_000)) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Unit
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                    else -> if (outputIndex >= 0) {
                        val buffer = codec.getOutputBuffer(outputIndex)
                        if (buffer != null && info.size > 0) {
                            buffer.position(info.offset)
                            buffer.limit(info.offset + info.size)
                            val pcm = buffer.slice().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                            val frameCount = min(pcm.remaining() / channels, output.size - write)
                            repeat(frameCount) {
                                var sum = 0f
                                repeat(channels) { sum += pcm.get() / 32768f }
                                output[write++] = (sum / channels).coerceIn(-1f, 1f)
                            }
                        }
                        codec.releaseOutputBuffer(outputIndex, false)
                        if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) outputDone = true
                    }
                }
            }
        } finally {
            runCatching { codec.stop() }
            codec.release()
            extractor.release()
        }

        val duration = if (durationUs > 0) durationUs / 1_000_000.0 else write.toDouble() / sampleRate
        return DecodedAudio(output.copyOf(write), sampleRate, channels, min(duration, maxSeconds))
    }
}

object SignalFeatures {
    fun rmsEnvelope(audio: FloatArray, frameSize: Int = 2048, hop: Int = 512): FloatArray {
        if (audio.size < frameSize) return FloatArray(0)
        val count = 1 + (audio.size - frameSize) / hop
        return FloatArray(count) { frame ->
            val start = frame * hop
            var sum = 0.0
            for (i in 0 until frameSize) {
                val x = audio[start + i].toDouble()
                sum += x * x
            }
            sqrt(sum / frameSize).toFloat()
        }
    }

    fun chromaFrames(audio: FloatArray, sampleRate: Int, frameSize: Int = 4096, hop: Int = 2048): Array<DoubleArray> {
        if (audio.size < frameSize) return emptyArray()
        val frames = ((audio.size - frameSize) / hop + 1).coerceAtMost(1800)
        val output = Array(frames) { DoubleArray(12) }
        val real = DoubleArray(frameSize)
        val imag = DoubleArray(frameSize)
        val window = DoubleArray(frameSize) { n -> 0.5 - 0.5 * cos(2.0 * PI * n / (frameSize - 1)) }

        for (frame in 0 until frames) {
            val start = frame * hop
            java.util.Arrays.fill(real, 0.0)
            java.util.Arrays.fill(imag, 0.0)
            for (n in 0 until frameSize) real[n] = audio[start + n] * window[n]
            fft(real, imag)

            val chroma = output[frame]
            for (k in 1 until frameSize / 2) {
                val freq = k.toDouble() * sampleRate / frameSize
                if (freq !in 55.0..1760.0) continue
                val mag = hypot(real[k], imag[k])
                if (mag < 1e-6) continue
                val midi = (69.0 + 12.0 * log2(freq / 440.0)).roundToInt()
                val pc = ((midi % 12) + 12) % 12
                chroma[pc] += mag
            }
            val max = chroma.maxOrNull() ?: 0.0
            if (max > 0.0) for (i in 0 until 12) chroma[i] /= max
        }
        return output
    }

    fun chroma(audio: FloatArray, sampleRate: Int, frameSize: Int = 4096, hop: Int = 2048): DoubleArray {
        val frames = chromaFrames(audio, sampleRate, frameSize, hop)
        if (frames.isEmpty()) return DoubleArray(12)
        val result = DoubleArray(12)
        for (frame in frames) for (pc in 0 until 12) result[pc] += frame[pc]
        val max = result.maxOrNull() ?: 0.0
        return if (max > 0) result.map { it / max }.toDoubleArray() else result
    }

    private fun fft(real: DoubleArray, imag: DoubleArray) {
        val n = real.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while ((j and bit) != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) {
                val tr = real[i]; real[i] = real[j]; real[j] = tr
                val ti = imag[i]; imag[i] = imag[j]; imag[j] = ti
            }
        }
        var len = 2
        while (len <= n) {
            val angle = -2.0 * PI / len
            val wLenR = cos(angle)
            val wLenI = sin(angle)
            var i = 0
            while (i < n) {
                var wr = 1.0
                var wi = 0.0
                for (k in 0 until len / 2) {
                    val uR = real[i + k]
                    val uI = imag[i + k]
                    val vR = real[i + k + len / 2] * wr - imag[i + k + len / 2] * wi
                    val vI = real[i + k + len / 2] * wi + imag[i + k + len / 2] * wr
                    real[i + k] = uR + vR
                    imag[i + k] = uI + vI
                    real[i + k + len / 2] = uR - vR
                    imag[i + k + len / 2] = uI - vI
                    val nextWr = wr * wLenR - wi * wLenI
                    wi = wr * wLenI + wi * wLenR
                    wr = nextWr
                }
                i += len
            }
            len = len shl 1
        }
    }
}
