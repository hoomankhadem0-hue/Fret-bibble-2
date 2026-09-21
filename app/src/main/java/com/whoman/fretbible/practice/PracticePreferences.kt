package com.whoman.fretbible.practice

import android.content.Context

enum class PracticeMode(val label: String) {
    ADAPTIVE("Adaptive"),
    PATTERN("Pattern Training"),
    RANDOM("Full Random")
}

data class PracticeConfig(
    val count: Int = 24,
    val maxFret: Int = 21,
    val mode: PracticeMode = PracticeMode.ADAPTIVE,
    val timerSeconds: Int? = 10
) {
    val timerEnabled: Boolean get() = timerSeconds != null
}

object PracticePreferences {
    private const val PREFS = "fret_bible_practice_preferences"

    fun load(context: Context): PracticeConfig {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val mode = runCatching {
            PracticeMode.valueOf(p.getString("mode", PracticeMode.ADAPTIVE.name)!!)
        }.getOrDefault(PracticeMode.ADAPTIVE)

        val storedTimer = p.getInt("timerSeconds", 10)
        val timer = if (storedTimer <= 0) null else storedTimer.coerceIn(1, 600)

        return PracticeConfig(
            count = p.getInt("count", 24).coerceIn(5, 100),
            maxFret = p.getInt("maxFret", 21).coerceIn(1, 21),
            mode = mode,
            timerSeconds = timer
        )
    }

    fun save(context: Context, c: PracticeConfig) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("count", c.count.coerceIn(5, 100))
            .putInt("maxFret", c.maxFret.coerceIn(1, 21))
            .putString("mode", c.mode.name)
            .putInt("timerSeconds", c.timerSeconds?.coerceIn(1, 600) ?: 0)
            .apply()
    }
}
