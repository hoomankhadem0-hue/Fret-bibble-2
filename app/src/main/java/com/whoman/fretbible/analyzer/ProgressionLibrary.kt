package com.whoman.fretbible.analyzer

data class ProgressionPattern(val name:String,val genre:String,val numerals:List<String>,val weight:Double=1.0)

object ProgressionLibrary {
 private val noteNames=listOf("C","C#","D","Eb","E","F","F#","G","Ab","A","Bb","B")
 val patterns=listOf(
  p("Four Chord Pop","Pop","I V vi IV"),p("Sad Rotation","Pop","vi IV I V"),p("Pachelbel","Pop","I V vi iii IV I IV V"),p("50s Progression","Pop","I vi IV V"),p("Doo Wop","Pop","I vi ii V"),p("Simple Pop","Pop","I IV V I"),p("Minor Pop","Pop","i VI III VII"),
  p("Rock Backbone","Rock","I V IV I"),p("Rocking Loop","Rock","I V IV V"),p("Mixolydian Rock","Rock","I ♭VII IV"),p("Rock Minor","Rock","i ♭VII ♭VI ♭VII"),p("Grunge Loop","Rock","i ♭VI ♭III ♭VII"),
  p("12 Bar Blues","Blues","I I I I IV IV I I V IV I I",1.4),p("Jazz Blues","Blues","I7 IV7 I7 I7 IV7 IV7 I7 VI7 ii7 V7 I7 V7"),
  p("ii V I","Jazz","ii V I",1.5),p("Minor ii V i","Jazz","iiø V i",1.5),p("I vi ii V","Jazz","I vi ii V"),p("Backdoor","Jazz","iv ♭VII I"),p("Circle of Fifths","Jazz","vi ii V I"),p("Major 251","Jazz","ii7 V7 Imaj7"),p("Minor 251","Jazz","iiø7 V7 i"),
  p("Soul Loop","Soul","I vi IV V"),p("Neo Soul","Soul","Imaj7 III7 vi7 ii7"),p("R&B Axis","R&B","I V vi IV"),p("R&B Minor","R&B","i ♭VI ♭III ♭VII"),p("Funk Vamp","Funk","i7 IV7"),p("Funk Dominant","Funk","I7 IV7"),
  p("Gospel Turn","Gospel","I IV I V vi IV ii V"),p("Amen","Gospel","IV I IV I"),p("Country I IV V","Country","I IV V I"),p("Country 1","Country","I V vi IV"),p("Folk Loop","Folk","I V vi IV"),p("Folk Ballad","Folk","I vi IV V"),
  p("Latin Pop","Latin","i VI III VII"),p("Andalusian","Latin","i ♭VII ♭VI V"),p("Bossa Basic","Latin","Imaj7 vi7 ii7 V7"),p("Tango Minor","Latin","i iv V i"),p("Aeolian Metal","Metal","i ♭VI ♭III ♭VII"),p("Metal Descent","Metal","i ♭VII ♭VI V"),
  p("Phrygian Metal","Metal","i ♭II i ♭VII"),p("Dorian Metal","Metal","i IV VII i"),p("Cinematic Minor","Cinematic","i ♭VI ♭III ♭VII"),p("Heroic","Cinematic","I V vi IV"),p("Modal Dorian","Modal","i IV i VII"),p("Modal Mixolydian","Modal","I ♭VII IV I"),
  p("Authentic Cadence","Classical","V I",1.5),p("Plagal Cadence","Classical","IV I",1.5),p("Half Cadence","Classical","ii V"),p("Deceptive Cadence","Classical","V vi"),p("Descending Fifths","Classical","I IV vii iii vi ii V I"),
  p("Indie Dream","Indie","I V vi IV"),p("Indie Minor","Indie","i VI III VII"),p("Alternative Minor","Alternative","i ♭VI ♭III V"),p("Dream Pop","Alternative","I vi IV V"),
  p("I IV","Common","I IV",1.2),p("V I","Common","V I",1.5),p("I V","Common","I V",1.2),p("vi IV","Common","vi IV",1.2),p("ii V","Common","ii V",1.2),p("ii V I","Common","ii V I",1.5),p("vi ii V","Common","vi ii V"),p("i VII","Common","i VII",1.2),p("i iv V","Common","i iv V")
 ).map{it.copy(numerals=it.numerals.map(::normalize))}
 private fun p(n:String,g:String,s:String,w:Double=1.0)=ProgressionPattern(n,g,s.split(" "),w)
 private fun normalize(v:String)=when(v){"bII"->"♭II";"bIII"->"♭III";"bVI"->"♭VI";"bVII"->"♭VII";else->v}
 private val transitionWeights:Map<String,Map<String,Double>> by lazy{val m=mutableMapOf<String,MutableMap<String,Double>>();patterns.forEach{p->p.numerals.zipWithNext().forEach{(a,b)->m.getOrPut(a){mutableMapOf()}[b]=(m[a]?.get(b)?:0.0)+p.weight}};m}
 fun transitionPrior(previous:String?,candidate:String):Double{if(previous==null)return 0.0;val next=transitionWeights[previous]?.get(candidate)?:return 0.0;val total=transitionWeights[previous]?.values?.sum()?:0.0;return if(total>0)(next/total).coerceIn(0.0,1.0)else 0.0}
 fun romanFor(symbol:String,key:String):String?{val kp=key.trim().split(Regex("\\s+"));if(kp.isEmpty())return null;val rn=noteNames.sortedByDescending{it.length}.firstOrNull{symbol.trim().startsWith(it)}?:return null;val root=noteNames.indexOf(rn);val kr=noteNames.indexOf(kp.first());if(root<0||kr<0)return null;val scale=if(kp.getOrNull(1).equals("Minor",true))intArrayOf(0,2,3,5,7,8,10)else intArrayOf(0,2,4,5,7,9,11);val d=scale.indexOf((root-kr+12)%12);if(d<0)return null;val r=arrayOf("I","ii","iii","IV","V","vi","vii°")[d];return if(symbol.contains("m")&&!symbol.contains("maj",true))r.lowercase()else r}
}