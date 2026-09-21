package com.whoman.fretbible.practice

import android.content.Context

data class PracticeTotals(val sessions:Int,val points:Int,val correct:Int,val attempts:Int,val bestSessionPoints:Int,val practiceSeconds:Long){
    val accuracy:Int get()=if(attempts==0)0 else correct*100/attempts
}
object PracticeStatsStore{
    private const val PREFS="fret_bible_stats"
    fun load(context:Context):PracticeTotals{
        val p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
        return PracticeTotals(p.getInt("sessions",0),p.getInt("points",0),p.getInt("correct",0),p.getInt("attempts",0),p.getInt("best",0),p.getLong("seconds",0L))
    }
    fun saveSession(context:Context,points:Int,correct:Int,attempts:Int,seconds:Long){
        val old=load(context)
        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
            .putInt("sessions",old.sessions+1).putInt("points",old.points+points).putInt("correct",old.correct+correct)
            .putInt("attempts",old.attempts+attempts).putInt("best",maxOf(old.bestSessionPoints,points))
            .putLong("seconds",old.practiceSeconds+seconds.coerceAtLeast(0)).apply()
    }
}