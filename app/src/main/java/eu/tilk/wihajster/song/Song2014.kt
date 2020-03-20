package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import eu.tilk.wihajster.Event as TEvent

@JacksonXmlRootElement(localName = "Song")
class Song2014 {
    @JacksonXmlProperty(localName = "title")
    lateinit var title: String
    @JacksonXmlProperty(localName = "arrangement")
    lateinit var arrangement: String
    @JacksonXmlProperty(localName = "part")
    var part: Int = -1
    @JacksonXmlProperty(localName = "offset")
    var offset: Float = 0F
    @JacksonXmlProperty(localName = "centOffset")
    lateinit var centOffset: String
    @JacksonXmlProperty(localName = "songLength")
    var songLength: Float = 0F
    @JacksonXmlProperty(localName = "songNameSort")
    lateinit var songNameSort: String
    @JacksonXmlProperty(localName = "startBeat")
    var startBeat: Float = 0F
    @JacksonXmlProperty(localName = "averageTempo")
    var averageTempo: Float = 0F
    @JacksonXmlProperty(localName = "tuning")
    lateinit var tuning: Tuning
    @JacksonXmlProperty(localName = "capo")
    var capo: Int = -1
    @JacksonXmlProperty(localName = "artistName")
    lateinit var artistName: String
    @JacksonXmlProperty(localName = "artistNameSort")
    lateinit var artistNameSort: String
    @JacksonXmlProperty(localName = "albumName")
    lateinit var albumName: String
    @JacksonXmlProperty(localName = "albumNameSort")
    lateinit var albumNameSort: String
    @JacksonXmlProperty(localName = "albumYear")
    lateinit var albumYear: String
    @JacksonXmlProperty(localName = "albumArt")
    lateinit var albumArt: String
    @JacksonXmlProperty(localName = "crowdSpeed")
    lateinit var crowdSpeed: String
    @JacksonXmlProperty(localName = "arrangementProperties")
    lateinit var arrangementProperties: ArrangementProperties2014
    @JacksonXmlProperty(localName = "lastConversionDateTime")
    lateinit var lastConversionDateTime: String
    @JacksonXmlProperty(localName = "phrase")
    @JacksonXmlElementWrapper(localName = "phrases")
    var phrases: List<Phrase> = ArrayList()
    @JacksonXmlProperty(localName = "newLinkedDiff")
    @JacksonXmlElementWrapper(localName = "newLinkedDiffs")
    var newLinkedDiffs: List<NewLinkedDiff> = ArrayList()
    @JacksonXmlProperty(localName = "phraseIteration")
    @JacksonXmlElementWrapper(localName = "phraseIterations")
    var phraseIterations: List<PhraseIteration2014> = ArrayList()
    @JacksonXmlProperty(localName = "linkedDiff")
    @JacksonXmlElementWrapper(localName = "linkedDiffs")
    var linkedDiffs: List<LinkedDiff> = ArrayList()
    @JacksonXmlProperty(localName = "phraseProperty")
    @JacksonXmlElementWrapper(localName = "phraseProperties")
    var phraseProperties: List<PhraseProperty> = ArrayList()
    @JacksonXmlProperty(localName = "chordTemplate")
    @JacksonXmlElementWrapper(localName = "chordTemplates")
    var chordTemplates: List<ChordTemplate2014> = ArrayList()
    @JacksonXmlProperty(localName = "ebeat")
    @JacksonXmlElementWrapper(localName = "ebeats")
    var ebeats: List<EBeat> = ArrayList()
    @JacksonXmlProperty(localName = "tonebase")
    var toneBase: String? = null
    @JacksonXmlProperty(localName = "tonea")
    var toneA: String? = null
    @JacksonXmlProperty(localName = "toneb")
    var toneB: String? = null
    @JacksonXmlProperty(localName = "tonec")
    var toneC: String? = null
    @JacksonXmlProperty(localName = "toned")
    var toneD: String? = null
    @JacksonXmlProperty(localName = "tone")
    @JacksonXmlElementWrapper(localName = "tones")
    var tones: List<Tone2014> = ArrayList()
    @JacksonXmlProperty(localName = "section")
    @JacksonXmlElementWrapper(localName = "sections")
    var sections: List<Section> = ArrayList()
    @JacksonXmlProperty(localName = "event")
    @JacksonXmlElementWrapper(localName = "events")
    var events: List<Event> = ArrayList()
    @JacksonXmlProperty(localName = "control")
    @JacksonXmlElementWrapper(localName = "controls")
    var controls: List<Control> = ArrayList()
    @JacksonXmlProperty(localName = "transcriptionTrack")
    lateinit var transcriptionTrack: TranscriptionTrack2014
    @JacksonXmlElementWrapper(localName = "levels")
    @JacksonXmlProperty(localName = "level")
    var levels: List<Level2014> = ArrayList()
    fun makeEventListForInterval(level : Int, startTime : Float, endTime : Float) : List<TEvent> {
        val list = ArrayList<TEvent>()
        // inefficient, TODO binary search
        for (ebeat in ebeats) {
            if (ebeat.time >= startTime && ebeat.time < endTime)
                list.add(TEvent.Beat(ebeat.time))
        }
        for (note in levels[level].notes) {
            if (note.time >= startTime && note.time < endTime)
                list.add(TEvent.Note(
                    note.time,
                    note.fret,
                    note.string
                ))
        }
        for (chord in levels[level].chords) {
            if (chord.time >= startTime && chord.time < endTime)
                for (note in chord.chordNotes)
                    list.add(TEvent.Note(
                        note.time,
                        note.fret,
                        note.string
                    ))
        }
        list.sortBy { it.time }
        return list
    }
    fun makeEventListForLevels(phraseLevels : List<Int>) : List<TEvent> {
        val list = ArrayList<TEvent>()
        for (i in 0 until phraseIterations.size-1) {
            list.addAll(makeEventListForInterval(
                phraseLevels[phraseIterations[i].phraseId],
                phraseIterations[i].time,
                phraseIterations[i+1].time
            ))
        }
        return list
    }
    fun makeEventList() : List<TEvent> {
        val phraseLevels = ArrayList<Int>()
        for (phrase in phrases) {
            phraseLevels.add(phrase.maxDifficulty)
        }
        return makeEventListForLevels(phraseLevels)
    }
}