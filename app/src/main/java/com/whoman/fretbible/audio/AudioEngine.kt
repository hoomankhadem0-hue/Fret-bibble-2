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

class AudioEngine {
    companion object { const val SAMPLE_RATE = 44_100 }
    private val detector = PitchDetector(SAMPLE_RATE)
    private val running = AtomicBoolean(false)
    private var recorder: AudioRecord? = null
    private val _detected = MutableStateFlow<DetectedNote?>(null)
    val detected: StateFlow<DetectedNote?> = _detected

    suspend fun start() {
        if (running.getAndSet(true)) return
        val minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val bufferSize = (minBuffer * 2).coerceAtLeast(8192)
        recorder = try {
            AudioRecord(MediaRecorder.AudioSource.UNPROCESSED, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        } catch (_: Throwable) {
            AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        }
        withContext(Dispatchers.IO) {
            val local = recorder ?: return@withContext
            try {
                local.startRecording()
                val buffer = ShortArray(4096)
                while (running.get()) {
                    val count = local.read(buffer, 0, buffer.size)
                    if (count > 0) _detected.value = detector.detect(FloatArray(count) { i -> buffer[i] / 32768f })
                }
            } finally {
                try { local.stop() } catch (_: Throwable) {}
            }
        }
    }

    fun stop() {
        running.set(false)
        try { recorder?.stop() } catch (_: Throwable) {}
        recorder?.release()
        recorder = null
        _detected.value = null
    }
}
