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

package eu.tilk.cdlcplayer.song

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import eu.tilk.cdlcplayer.viewer.Effect
import eu.tilk.cdlcplayer.viewer.Event as TEvent

@JacksonXmlRootElement(localName = "Song")
class Song2014 {
    @JacksonXmlProperty(localName = "persistentID")
    var persistentID: String = ""
    @JacksonXmlProperty(localName = "songKey")
    var songKey: String = ""
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
    lateinit var arrangementProperties: ArrangementProperties2014
    @JacksonXmlProperty(localName = "lastConversionDateTime")
    var lastConversionDateTime: String = ""
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "phrase")
    @JacksonXmlElementWrapper(localName = "phrases")
    var phrases: List<Phrase> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "newLinkedDiff")
    @JacksonXmlElementWrapper(localName = "newLinkedDiffs")
    var newLinkedDiffs: List<NewLinkedDiff> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "phraseIteration")
    @JacksonXmlElementWrapper(localName = "phraseIterations")
    var phraseIterations: List<PhraseIteration2014> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "linkedDiff")
    @JacksonXmlElementWrapper(localName = "linkedDiffs")
    var linkedDiffs: List<LinkedDiff> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "phraseProperty")
    @JacksonXmlElementWrapper(localName = "phraseProperties")
    var phraseProperties: List<PhraseProperty> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "chordTemplate")
    @JacksonXmlElementWrapper(localName = "chordTemplates")
    var chordTemplates: List<ChordTemplate2014> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
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
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "tone")
    @JacksonXmlElementWrapper(localName = "tones")
    var tones: List<Tone2014> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "section")
    @JacksonXmlElementWrapper(localName = "sections")
    var sections: List<Section> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "event")
    @JacksonXmlElementWrapper(localName = "events")
    var events: List<Event> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "control")
    @JacksonXmlElementWrapper(localName = "controls")
    var controls: List<Control> = ArrayList()
    @JacksonXmlProperty(localName = "transcriptionTrack")
    var transcriptionTrack: TranscriptionTrack2014? = null
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlElementWrapper(localName = "levels")
    @JacksonXmlProperty(localName = "level")
    var levels: List<Level2014> = ArrayList()
    fun makeEventListForInterval(level : Int, startTime : Float, endTime : Float) : List<TEvent> {
        val list = ArrayList<TEvent>()
        // inefficient, TODO binary search
        val guardAnchor = Anchor2014(endTime)
        for ((anchor, anchor2) in (levels[level].anchors
            .filter { it.time >= startTime && it.time < endTime } + listOf(guardAnchor))
            .zipWithNext()) {
            list.add(TEvent.Anchor(anchor.time, anchor.fret, anchor.width, anchor2.time))
        }
        for (ebeat in ebeats) {
            if (ebeat.time >= startTime && ebeat.time < endTime)
                list.add(TEvent.Beat(ebeat.time, ebeat.measure))
        }
        fun effectFrom(note : Note2014) =
            when {
                note.accent > 0 -> Effect.Accent
                note.hammerOn > 0 -> Effect.HammerOn
                note.pullOff > 0 -> Effect.PullOff
                note.palmMute > 0 -> Effect.PalmMute
                note.mute > 0 -> Effect.FrethandMute
                note.harmonic > 0 -> Effect.Harmonic
                note.harmonicPinch > 0 -> Effect.PinchHarmonic
                note.tap > 0 -> Effect.Tap
                note.slap > 0 -> Effect.Slap
                else -> null
            }
        fun effectFromChord(chord : Chord2014) =
            when {
                chord.palmMute > 0 -> Effect.PalmMute
                chord.fretHandMute > 0 -> Effect.FrethandMute
                else -> null
            }
        fun singleNoteFrom(note : Note2014, derived : Boolean) =
            TEvent.Note(
                note.time,
                note.fret,
                note.string,
                note.leftHand,
                derived,
                note.bendValues.firstOrNull { bv -> bv.time <= note.time }?.step ?: 0f,
                effectFrom(note)
            )
        fun noteFrom(note : Note2014, derived : Boolean) = if (note.linked <= 0)
            sequenceOf(singleNoteFrom(note, derived)) else emptySequence()
        fun noteSustainFrom(note : Note2014) = if (note.sustain > 0)
            sequenceOf(TEvent.NoteSustain(
                note.time,
                note.fret,
                note.string,
                note.leftHand,
                note.sustain,
                note.slideTo,
                note.slideUnpitchTo,
                note.tremolo > 0,
                note.linked > 0,
                note.vibrato,
                note.bendValues.map { bv ->
                    Pair(
                        (bv.time - note.time) / note.sustain,
                        bv.step
                    )
                }
            )) else emptySequence()
        fun noteEventsFrom(note : Note2014, derived : Boolean) =
            noteFrom(note, derived) + noteSustainFrom(note)
        for (note in levels[level].notes) {
            if (note.time >= startTime && note.time < endTime)
                list.addAll(noteEventsFrom(note, false))
        }
        for (handShape in levels[level].handShapes) {
            if (handShape.startTime >= startTime && handShape.startTime < endTime)
                list.add(TEvent.HandShape(handShape.startTime,
                    handShape.endTime - handShape.startTime))
        }
        var lastChordId = -1
        var noteidx = 0
        for (chord in levels[level].chords) {
            // if note between chords, chord not repeated
            while (noteidx < levels[level].notes.size
                && levels[level].notes[noteidx].time < chord.time) {
                noteidx++
                lastChordId = -1
            }
            val chordTpl = chordTemplates[chord.chordId]
            if (chord.time >= startTime && chord.time < endTime) {
                val repeated = chord.chordId == lastChordId && chord.chordNotes.isEmpty()
                val notes = mutableListOf<TEvent.Note>()
                if (chord.chordNotes.isNotEmpty())
                    for (note in chord.chordNotes) {
                        notes.add(singleNoteFrom(note, true))
                        if (!repeated) list.addAll(noteEventsFrom(note, true))
                    }
                else
                    for (stringNo in 0..5) if (chordTpl.fret[stringNo] >= 0) {
                        val note = TEvent.Note(
                            chord.time,
                            chordTpl.fret[stringNo],
                            stringNo.toByte(),
                            chordTpl.finger[stringNo],
                            true
                        )
                        notes.add(note)
                        if (!repeated) list.add(note)
                    }
                list.add(TEvent.Chord(chord.time, chord.chordId, notes, repeated, effectFromChord(chord)))
            }
            lastChordId = chord.chordId
        }
        list.sortBy { it.time }
        return list
    }
    fun makeEventListForLevels(phraseLevels : List<Int>) : List<TEvent> {
        val list = ArrayList<TEvent>()
        list.add(TEvent.Anchor(0f, 1, 0,
            levels[phraseLevels[phraseIterations[0].phraseId]].anchors.first().time))
        for (i in 0 until phraseIterations.size-1) {
            list.addAll(makeEventListForInterval(
                phraseLevels[phraseIterations[i].phraseId],
                phraseIterations[i].time,
                phraseIterations[i+1].time
            ))
        }
        list.add(TEvent.Anchor(phraseIterations.last().time, 1, 0, songLength))
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