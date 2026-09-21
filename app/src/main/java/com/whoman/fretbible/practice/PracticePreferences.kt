package com.whoman.fretbible.practice
import android.content.Context
enum class PracticeMode(val label:String){ADAPTIVE("Adaptive"),PATTERN("Pattern Training"),RANDOM("Full Random")}
data class PracticeConfig(val count:Int=24,val maxFret:Int=21,val mode:PracticeMode=PracticeMode.ADAPTIVE,val timerEnabled:Boolean=true)
object PracticePreferences{
 private const val PREFS="fret_bible_practice_preferences"
 fun load(context:Context):PracticeConfig{val p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);val mode=runCatching{PracticeMode.valueOf(p.getString("mode",PracticeMode.ADAPTIVE.name)!!)}.getOrDefault(PracticeMode.ADAPTIVE);return PracticeConfig(p.getInt("count",24).coerceIn(5,100),p.getInt("maxFret",21).coerceIn(12,21),mode,p.getBoolean("timer",true))}
 fun save(context:Context,c:PracticeConfig){context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putInt("count",c.count.coerceIn(5,100)).putInt("maxFret",c.maxFret.coerceIn(12,21)).putString("mode",c.mode.name).putBoolean("timer",c.timerEnabled).apply()}
}