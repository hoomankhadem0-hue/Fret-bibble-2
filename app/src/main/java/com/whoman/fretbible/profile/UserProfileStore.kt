package com.whoman.fretbible.profile

import android.content.Context

object UserProfileStore {
    private const val PREFS = "fret_bible_user"
    private const val NAME = "display_name"

    fun loadName(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(NAME, "")
            .orEmpty()

    fun saveName(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(NAME, name.trim().take(24))
            .apply()
    }
}
