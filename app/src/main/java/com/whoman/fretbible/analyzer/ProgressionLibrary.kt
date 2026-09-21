package com.whoman.fretbible.analyzer

/**
 * Offline harmonic prior for the Analyzer.
 *
 * Patterns are Roman-numeral progressions. They are used as a soft prior,
 * never as a hard rule, so unusual harmony can still be reported.
 */
data class ProgressionPattern(
    val name: String,
    val genre: String,
    val numerals: List<String>,
    val weight: Double = 1.0
)

object ProgressionLibrary {
    private val noteNames = listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")
    private val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    val patterns: List<ProgressionPattern> = listOf(
        p("Four Chord Pop","Pop","I V vi IV"), p("Sad Rotation","Pop","vi IV I V"),
        p("Axis Rotation","Pop","I V vi iii IV I IV V"), p("Pop Loop","Pop","I vi IV V"),
        p("50s Progression","Pop","I vi IV V"), p("Doo Wop","Pop","I vi ii V"),
        p("Pachelbel","Pop","I V vi iii IV I IV V"), p("Pachelbel Short","Pop","I V vi iii IV V"),
        p("Royal Road","Pop","IV V iii vi ii V I"), p("Pop Lift","Pop","IV V vi I"),
        p("Pop Descent","Pop","I V vi iii IV I ii V"), p("Anthem","Pop","I IV vi V"),
        p("Hopeful Turn","Pop","I IV vi V"), p("Ascending Pop","Pop","I ii iii IV V"),
        p("Descending Thirds","Pop","I iii vi IV"), p("Mediant Turnaround","Pop","I iii vi V"),
        p("Bittersweet","Pop","I V vi ii"), p("Simple Pop","Pop","I IV V I"),
        p("Plagal Pop","Pop","I IV I V"), p("Extended Axis","Pop","I V vi IV I V vi IV"),
        p("Pop Cadence","Pop","vi IV V I"), p("Prechorus Lift","Pop","ii IV V I"),
        p("Minor Pop","Pop","i VI III VII"), p("Minor Wheel","Pop","i VII VI VII"),
        p("Minor Cascade","Pop","i VI III V"), p("Minor Resolve","Pop","i iv VII III"),
        p("Minor Anthem","Pop","i VI VII III"), p("Aeolian Loop","Pop","i VII VI III"),
        p("Minor Four Chord","Pop","i VI iv V"), p("Minor Pop Axis","Pop","i VI III VII"),
        p("Rock Backbone","Rock","I V IV I"), p("Rocking Loop","Rock","I V IV V"),
        p("Classic Rock","Rock","I IV V IV"), p("Rock Turnaround","Rock","I IV I V"),
        p("Power Chord Cycle","Rock","I ♭VII IV I"), p("Mixolydian Rock","Rock","I ♭VII IV"),
        p("Rock Minor","Rock","i ♭VII ♭VI ♭VII"), p("Minor Rock","Rock","i VI VII i"),
        p("Grunge Loop","Rock","i ♭VI ♭III ♭VII"), p("Alternative Loop","Rock","I ♭III IV I"),
        p("Chromatic Rock","Rock","I ♭III IV iv"), p("Rock Resolve","Rock","IV ♭VII I"),
        p("Two Chord Rock","Rock","I IV"), p("Two Chord Minor Rock","Rock","i VII"),
        p("Blues Rock","Rock","I IV I V IV I"), p("Arena Rock","Rock","I V IV vi"),
        p("Rock Anthem","Rock","I IV V vi"), p("Descending Rock","Rock","I V IV iii"),
        p("Modal Rock","Rock","i ♭VII IV ♭VII"), p("Rock Bridge","Rock","vi IV I V"),
        p("12 Bar Blues","Blues","I I I I IV IV I I V IV I I",1.4),
        p("12 Bar Blues Variant","Blues","I I I I IV IV I V I I V IV",1.2),
        p("Quick IV Blues","Blues","I IV I I IV IV I I V IV I I"),
        p("Blues Turnaround","Blues","I VI ii V"), p("Blues Bird","Blues","I VI II V"),
        p("Minor Blues","Blues","i i i i iv iv i i V iv i i"),
        p("Jazz Blues","Blues","I7 IV7 I7 I7 IV7 IV7 I7 VI7 ii7 V7 I7 V7"),
        p("Blues Ending","Blues","IV I V IV I"), p("Dominant Blues","Blues","I7 IV7 V7 IV7"),
        p("ii V I","Jazz","ii V I",1.5), p("Minor ii V i","Jazz","iiø V i",1.5),
        p("I vi ii V","Jazz","I vi ii V"), p("Turnaround","Jazz","I vi ii V"),
        p("Backdoor","Jazz","iv ♭VII I"), p("Tritone Backdoor","Jazz","ii ♭II I"),
        p("Coltrane-ish Cycle","Jazz","I V/vi vi V/ii ii V I"),
        p("Circle of Fifths","Jazz","vi ii V I"), p("Extended Circle","Jazz","iii vi ii V I"),
        p("Jazz Cadence","Jazz","IV iv I"), p("Minor Turnaround","Jazz","i viø iiø V"),
        p("Rhythm Changes A","Jazz","I vi ii V"), p("Rhythm Changes Bridge","Jazz","III VI II V"),
        p("Dominant Chain","Jazz","III7 VI7 II7 V7"), p("Secondary Dominants","Jazz","I V/ii ii V I"),
        p("Major 251","Jazz","ii7 V7 Imaj7"), p("Minor 251","Jazz","iiø7 V7 i"),
        p("Jazz Plagal","Jazz","IVmaj7 ivm6 Imaj7"), p("Tonic Expansion","Jazz","I I7 IV iv I"),
        p("Gospel Jazz","Jazz","I III7 vi ii V I"), p("Coltrane Cycle","Jazz","I III7 VI7 II7 V7 I"),
        p("Soul Loop","Soul","I vi IV V"), p("Neo Soul","Soul","Imaj7 III7 vi7 ii7"),
        p("Neo Soul Minor","Soul","i7 iv7 ♭VImaj7 V7"), p("R&B Axis","R&B","I V vi IV"),
        p("R&B Minor","R&B","i ♭VI ♭III ♭VII"), p("R&B Turnaround","R&B","I vi ii V"),
        p("Funk Vamp","Funk","i7 IV7"), p("Funk Dominant","Funk","I7 IV7"),
        p("Soul Plagal","Soul","IV I V vi"), p("Soul Walkdown","Soul","I V/vi vi IV"),
        p("Gospel Turn","Gospel","I IV I V vi IV ii V"), p("Gospel Lift","Gospel","IV I V vi IV V I"),
        p("Gospel 251","Gospel","ii V I vi"), p("Amen","Gospel","IV I IV I"),
        p("Funk Minor Vamp","Funk","i7 ♭VII7"), p("Funk Major Vamp","Funk","I7 IV7 I7"),
        p("Country I IV V","Country","I IV V I"), p("Country 1","Country","I V vi IV"),
        p("Country Walk","Country","I V/vi vi IV"), p("Country Turn","Country","I IV I V"),
        p("Folk Loop","Folk","I V vi IV"), p("Folk Ballad","Folk","I vi IV V"),
        p("Folk Plagal","Folk","I IV I IV"), p("Folk Minor","Folk","i VII VI VII"),
        p("Singer Songwriter","Folk","I V IV I vi IV V"), p("Modal Folk","Folk","I ♭VII IV I"),
        p("Celtic","Folk","I V vi iii IV I IV V"), p("Descending Folk","Folk","I V vi IV iii"),
        p("Latin Pop","Latin","i VI III VII"), p("Latin Minor","Latin","i VII VI V"),
        p("Latin Cadence","Latin","iv V i"), p("Andalusian","Latin","i ♭VII ♭VI V"),
        p("Andalusian Variant","Latin","i VII VI V"), p("Latin Turnaround","Latin","I VI ii V"),
        p("Bossa Basic","Latin","Imaj7 vi7 ii7 V7"), p("Bossa Minor","Latin","i7 iv7 V7"),
        p("Tango Minor","Latin","i iv V i"), p("Tango Major","Latin","I IV V I"),
        p("Aeolian Metal","Metal","i ♭VI ♭III ♭VII"), p("Metal Descent","Metal","i ♭VII ♭VI V"),
        p("Phrygian Metal","Metal","i ♭II i ♭VII"), p("Metal Pedal","Metal","i ♭VI i ♭VII"),
        p("Dorian Metal","Metal","i IV VII i"), p("Power Metal","Metal","i VI III VII"),
        p("Cinematic Minor","Cinematic","i ♭VI ♭III ♭VII"), p("Cinematic Descent","Cinematic","i VII VI V"),
        p("Heroic","Cinematic","I V vi IV"), p("Epic Minor","Cinematic","i VI iv V"),
        p("Modal Dorian","Modal","i IV i VII"), p("Modal Mixolydian","Modal","I ♭VII IV I"),
        p("Modal Aeolian","Modal","i ♭VII ♭VI ♭VII"), p("Phrygian","Modal","i ♭II ♭VII i"),
        p("Authentic Cadence","Classical","V I",1.5), p("Plagal Cadence","Classical","IV I",1.5),
        p("Half Cadence","Classical","ii V"), p("Deceptive Cadence","Classical","V vi"),
        p("Predominant Dominant","Classical","ii V I"), p("Circle Sequence","Classical","vi ii V I"),
        p("Descending Fifths","Classical","I IV vii iii vi ii V I"), p("Descending Fifths Minor","Classical","i iv VII III VI iiø V i"),
        p("Romanesca","Classical","i VII VI V"), p("Passamezzo","Classical","i VII i V"),
        p("Chromatic Predominant","Classical","IV iv I"), p("Cadential 64","Classical","I6 V I"),
        p("Leading Tone Resolution","Classical","vii I"), p("Minor Authentic","Classical","V i"),
        p("Indie Dream","Indie","I V vi IV"), p("Indie Minor","Indie","i VI III VII"),
        p("Indie Plagal","Indie","I IV I ♭VII"), p("Indie Walk","Indie","I iii IV iv"),
        p("Alternative Minor","Alternative","i ♭VI ♭III V"), p("Alternative Major","Alternative","I ♭III IV iv"),
        p("Post Rock","Alternative","I V vi IV"), p("Dream Pop","Alternative","I vi IV V"),
        p("Shoegaze","Alternative","i VI III VII"), p("Emo","Alternative","I V vi IV"),
        p("Emo Minor","Alternative","i VI III VII"), p("Indie Cadence","Indie","IV V I"),
        p("I IV","Common","I IV",1.2), p("IV V","Common","IV V",1.2),
        p("V I","Common","V I",1.5), p("I V","Common","I V",1.2),
        p("vi IV","Common","vi IV",1.2), p("IV I","Common","IV I",1.2),
        p("ii V","Common","ii V",1.2), p("ii V I","Common","ii V I",1.5),
        p("I vi","Common","I vi",1.1), p("vi ii V","Common","vi ii V"),
        p("I iii vi","Common","I iii vi"), p("iii vi ii V","Common","iii vi ii V"),
        p("i VII","Common","i VII",1.2), p("i VI","Common","i VI",1.1),
        p("i iv V","Common","i iv V"), p("i ♭VI V","Common","i ♭VI V")
    ).map { pattern ->
        pattern.copy(numerals = pattern.numerals.map(::normalize))
    }

    private fun p(name: String, genre: String, pattern: String, weight: Double = 1.0) =
        ProgressionPattern(name, genre, pattern.split(" "), weight)

    private fun normalize(value: String): String = when (value) {
        "bII" -> "♭II"; "bIII" -> "♭III"; "bVI" -> "♭VI"; "bVII" -> "♭VII"
        else -> value
    }

    private val transitionWeights: Map<String, Map<String, Double>> by lazy {
        val map = mutableMapOf<String, MutableMap<String, Double>>()
        for (pattern in patterns) {
            pattern.numerals.zipWithNext().forEach { (a, b) ->
                map.getOrPut(a) { mutableMapOf() }[b] =
                    (map[a]?.get(b) ?: 0.0) + pattern.weight
            }
        }
        map
    }

    /** Soft transition prior. Returns 0..1 and deliberately stays modest. */
    fun transitionPrior(previous: String?, candidate: String): Double {
        if (previous == null) return 0.0
        val next = transitionWeights[previous]?.get(candidate) ?: return 0.0
        val total = transitionWeights[previous]?.values?.sum() ?: 0.0
        return if (total > 0) (next / total).coerceIn(0.0, 1.0) else 0.0
    }

    fun romanFor(symbol: String, key: String): String? {
        val keyParts = key.trim().split(Regex("\\s+"))
        if (keyParts.isEmpty()) return null
        val rootToken = symbol.trim()
        val rootName = noteNames.sortedByDescending { it.length }
            .firstOrNull { rootToken.startsWith(it) } ?: return null
        val root = noteNames.indexOf(rootName)
        val keyRoot = noteNames.indexOf(keyParts.first())
        if (root < 0 || keyRoot < 0) return null
        val mode = keyParts.getOrNull(1).orEmpty()
        val scale = if (mode.equals("Minor", true)) intArrayOf(0,2,3,5,7,8,10)
                   else intArrayOf(0,2,4,5,7,9,11)
        val degree = scale.indexOf((root - keyRoot + 12) % 12)
        if (degree < 0) return null
        val roman = arrayOf("I","ii","iii","IV","V","vi","vii°")[degree]
        val minor = rootToken.contains("m") && !rootToken.contains("maj", true)
        return if (minor) roman.lowercase() else roman
    }
}
