package com.whoman.fretbible.audio

object AudioSettings {
    // RMS gate. Lower values make quiet guitar notes detectable.
    @Volatile var sensitivity: Float = 0.00035f
}
