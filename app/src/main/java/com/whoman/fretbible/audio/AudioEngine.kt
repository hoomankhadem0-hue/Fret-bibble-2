package com.whoman.fretbible.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.whoman.fretbible.core.model.DetectedNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

class AudioEngine {
    companion object {
        const val SAMPLE_RATE = 44100
        private const val FRAME_SIZE = 4096
    }

    private val detector = PitchDetector(SAMPLE_RATE)
    private val running = AtomicBoolean(false)
    private var recorder: AudioRecord? = null

    private val _detected = MutableStateFlow<DetectedNote?>(null)
    val detected: StateFlow<DetectedNote?> = _detected

    private val _level = MutableStateFlow(0f)
    val level: StateFlow<Float> = _level

    suspend fun start() {
        if (running.getAndSet(true)) return

        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) {
            running.set(false)
            return
        }

        val bufferSize = (minBuffer * 2).coerceAtLeast(FRAME_SIZE * 2)

        // Some phones expose UNPROCESSED with extremely low gain.
        // MIC first gives those devices a much more usable input level.
        recorder = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
        } catch (_: Throwable) {
            try {
                AudioRecord(
                    MediaRecorder.AudioSource.UNPROCESSED,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
            } catch (_: Throwable) {
                null
            }
        }

        withContext(Dispatchers.IO) {
            val local = recorder ?: run {
                running.set(false)
                return@withContext
            }

            try {
                if (local.state != AudioRecord.STATE_INITIALIZED) {
                    running.set(false)
                    return@withContext
                }

                local.startRecording()
                val buffer = ShortArray(FRAME_SIZE)

                while (running.get()) {
                    val count = local.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)
                    if (!running.get()) break
                    if (count != FRAME_SIZE) continue

                    val samples = FloatArray(count) { i -> buffer[i] / 32768f }

                    var sum = 0.0
                    for (v in samples) sum += v.toDouble() * v.toDouble()
                    _level.value = sqrt(sum / count).toFloat()
                    _detected.value = detector.detect(samples)
                }
            } catch (_: Throwable) {
                // Treat unavailable input as silence instead of crashing practice.
            } finally {
                try { local.stop() } catch (_: Throwable) {}
            }
        }
    }

    fun stop() {
        running.set(false)
        val local = recorder
        recorder = null
        try { local?.stop() } catch (_: Throwable) {}
        try { local?.release() } catch (_: Throwable) {}
        _detected.value = null
        _level.value = 0f
    }
}
