/*
 *     Copyright (C) 2020  Marek Materzok
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import eu.tilk.wihajster.viewer.Event as TEvent

@JacksonXmlRootElement(localName = "Song")
class Song2014 {
    @JacksonXmlProperty(localName = "persistentID")
    var persistentID: String = ""
    @JacksonXmlProperty(localName = "title")
    var title: String = ""
    @JacksonXmlProperty(localName = "arrangement")
    var arrangement: String = ""
    @JacksonXmlProperty(localName = "part")
    var part: Int = -1
    @JacksonXmlProperty(localName = "offset")
    var offset: Float = 0F
    @JacksonXmlProperty(localName = "centOffset")
    var centOffset: Float = 0f
    @JacksonXmlProperty(localName = "songLength")
    var songLength: Float = 0F
    @JacksonXmlProperty(localName = "songNameSort")
    var songNameSort: String = ""
    @JacksonXmlProperty(localName = "startBeat")
    var startBeat: Float = 0F
    @JacksonXmlProperty(localName = "averageTempo")
    var averageTempo: Float = 0F
    @JacksonXmlProperty(localName = "tuning")
    lateinit var tuning: Tuning
    @JacksonXmlProperty(localName = "capo")
    var capo: Int = -1
    @JacksonXmlProperty(localName = "artistName")
    var artistName: String = ""
    @JacksonXmlProperty(localName = "artistNameSort")
    var artistNameSort: String = ""
    @JacksonXmlProperty(localName = "albumName")
    var albumName: String = ""
    @JacksonXmlProperty(localName = "albumNameSort")
    var albumNameSort: String = ""
    @JacksonXmlProperty(localName = "albumYear")
    var albumYear: Int = 0
    @JacksonXmlProperty(localName = "albumArt")
    var albumArt: String = ""
    @JacksonXmlProperty(localName = "crowdSpeed")
    var crowdSpeed: String = ""
    @JacksonXmlProperty(localName = "arrangementProperties")
    var arrangementProperties: ArrangementProperties2014? = null
    @JacksonXmlProperty(localName = "lastConversionDateTime")
    var lastConversionDateTime: String = ""
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
    var transcriptionTrack: TranscriptionTrack2014? = null
    @JacksonXmlElementWrapper(localName = "levels")
    @JacksonXmlProperty(localName = "level")
    var levels: List<Level2014> = ArrayList()
    fun makeEventListForInterval(level : Int, startTime : Float, endTime : Float) : List<TEvent> {
        val list = ArrayList<TEvent>()
        // inefficient, TODO binary search
        for (anchor in levels[level].anchors) {
            if (anchor.time >= startTime && anchor.time < endTime)
                list.add(TEvent.Anchor(anchor.time, anchor.fret, anchor.width))
        }
        for (ebeat in ebeats) {
            if (ebeat.time >= startTime && ebeat.time < endTime)
                list.add(TEvent.Beat(ebeat.time, ebeat.measure))
        }
        fun noteFrom(note : Note2014) = TEvent.Note(
            note.time,
            note.fret,
            note.string,
            note.sustain,
            note.slideTo,
            note.slideUnpitchTo,
            note.tremolo > 0,
            note.linked > 0,
            note.vibrato,
            note.bendValues.map { bv -> Pair((bv.time - note.time) / note.sustain, bv.step) }
        )
        for (note in levels[level].notes) {
            if (note.time >= startTime && note.time < endTime)
                list.add(noteFrom(note))
        }
        for (chord in levels[level].chords) {
            val chordTpl = chordTemplates[chord.chordId]
            if (chord.time >= startTime && chord.time < endTime)
                if (chord.chordNotes.isNotEmpty())
                    for (note in chord.chordNotes)
                        list.add(noteFrom(note))
                else
                    for (stringNo in 0..5) if (chordTpl.fret[stringNo] >= 0)
                        list.add(TEvent.Note(
                            chord.time,
                            chordTpl.fret[stringNo],
                            stringNo.toByte()
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