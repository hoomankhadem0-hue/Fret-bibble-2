package com.whoman.fretbible.analyzer

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
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
            val mime = f.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("audio/")) { track = i; break }
        }
        require(track >= 0) { "No audio track found" }
        extractor.selectTrack(track)
        val format = extractor.getTrackFormat(track)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: error("Unsupported audio")
        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else 0L
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
                        val input = codec.getInputBuffer(inputIndex) ?: continue
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
                val outputIndex = codec.dequeueOutputBuffer(info, 10_000)
                if (outputIndex >= 0) {
                    val buffer = codec.getOutputBuffer(outputIndex)
                    if (buffer != null && info.size > 0) {
                        buffer.position(info.offset)
                        buffer.limit(info.offset + info.size)
                        val remaining = min(buffer.remaining() / 2, (output.size - write) * channels)
                        var i = 0
                        while (i < remaining && write < output.size) {
                            var sum = 0f
                            var c = 0
                            while (c < channels && i + c < remaining) {
                                val lo = buffer.get(i + c).toInt() and 0xff
                                val hi = buffer.get(i + c + 1).toInt()
                                sum += ((hi shl 8) or lo).toShort() / 32768f
                                c++
                            }
                            output[write++] = sum / channels
                            i += channels * 2
                        }
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) outputDone = true
                }
            }
        } finally {
            codec.stop(); codec.release(); extractor.release()
        }
        return DecodedAudio(output.copyOf(write), sampleRate, channels, if (durationUs > 0) durationUs / 1_000_000.0 else write.toDouble()/sampleRate)
    }
}

object SignalFeatures {
    fun rmsEnvelope(audio: FloatArray, frameSize: Int = 2048, hop: Int = 512): FloatArray {
        if (audio.size < frameSize) return FloatArray(0)
        val count = 1 + (audio.size - frameSize) / hop
        return FloatArray(count) { frame ->
            val start = frame * hop
            var sum = 0.0
            for (i in 0 until frameSize) { val x=audio[start+i].toDouble(); sum += x*x }
            sqrt(sum / frameSize).toFloat()
        }
    }

    fun chroma(audio: FloatArray, sampleRate: Int, frameSize: Int = 4096, hop: Int = 2048): DoubleArray {
        if (audio.size < frameSize) return DoubleArray(12)
        val frames = ((audio.size - frameSize) / hop + 1).coerceAtMost(2400)
        val result = DoubleArray(12)
        for (f in 0 until frames) {
            val start=f*hop
            for (k in 1 until frameSize/2) {
                val freq=k.toDouble()*sampleRate/frameSize
                if (freq !in 60.0..1600.0) continue
                var re=0.0; var im=0.0
                for (n in 0 until frameSize) {
                    val x=audio[start+n].toDouble()*(0.5-0.5*cos(2*PI*n/(frameSize-1)))
                    val phase=2*PI*k*n/frameSize
                    re += x*cos(phase); im -= x*sin(phase)
                }
                val mag=sqrt(re*re+im*im)
                val midi=(69+12*log2(freq/440.0)).roundToInt()
                result[((midi%12)+12)%12] += mag
            }
        }
        val max=result.maxOrNull() ?: 0.0
        return if(max>0) result.map{it/max}.toDoubleArray() else result
    }
}
