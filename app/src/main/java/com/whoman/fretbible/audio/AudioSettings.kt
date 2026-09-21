package com.whoman.fretbible.audio

import android.content.Context

object AudioSettings {
    private const val PREFS = "fret_bible_audio"
    private const val KEY = "sensitivity"

    // Middle/default setting: users can tune this after installation.
    private const val DEFAULT_SENSITIVITY = 0.0015f

    @Volatile
    var sensitivity: Float = DEFAULT_SENSITIVITY
        private set

    fun load(context: Context) {
        sensitivity = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY, DEFAULT_SENSITIVITY)
            .coerceIn(0.00005f, 0.006f)
    }

    fun setSensitivity(context: Context, value: Float) {
        val safe = value.coerceIn(0.00005f, 0.006f)
        sensitivity = safe
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY, safe)
            .apply()
    }
}
