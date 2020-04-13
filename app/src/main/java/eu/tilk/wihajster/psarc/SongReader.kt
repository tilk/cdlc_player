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

package eu.tilk.wihajster.psarc

import eu.tilk.wihajster.manifest.Attributes
import eu.tilk.wihajster.song.*
import loggersoft.kotlin.streams.Stream

@ExperimentalUnsignedTypes
class SongReader(private val stream : Stream, private val attributes : Attributes) {

    private fun readEBeat() = stream.run {
        val time = readFloat()
        val measure = readShort()
        val beat = readShort()
        val phraseIteration = readInt()
        val mask = readInt()
        EBeat(time, if (mask != 0) measure else -1)
    }

    private fun <T> readMany(read : () -> T) = stream.run {
        val count = readInt()
        val list = ArrayList<T>(count)
        for (i in 0 until count) list.add(read())
        list
    }

    private fun readPhrase() = stream.run {
        val solo = readByte()
        val disparity = readByte()
        val ignore = readByte()
        val padding = readByte()
        val maxDifficulty = readInt()
        val phraseIterationLinks = readInt()
        val name = readString(length = 32).trimEnd('\u0000')
        Phrase(disparity, ignore, maxDifficulty, name, solo)
    }

    private fun readChordTemplate() = stream.run {
        val mask = readInt()
        val frets = Array(6) { readByte() }
        val fingers = Array(6) { readByte() }
        val notes = Array(6) { readInt() }
        val name = readString(length = 32).trimEnd('\u0000')
        // TODO: arpeggio and nop, see SongChordTemplate2014.parse
        ChordTemplate2014(
            name, name,
            frets[0], frets[1], frets[2], frets[3], frets[4], frets[5],
            fingers[0], fingers[1], fingers[2], fingers[3], fingers[4], fingers[5]
        )
    }

    private fun readPhraseIteration() = stream.run {
        val phraseId = readInt()
        val startTime = readFloat()
        val nextPhraseTime = readFloat()
        val difficulty = Array(3) { readInt() }
        val heroLevels =
            if (difficulty.contentEquals(arrayOf(0, 0, 0))) listOf()
            else List(3) { i -> HeroLevel(difficulty[i], i + 1) }
        PhraseIteration2014("", startTime, phraseId, heroLevels)
    }

    private fun readPhraseProperties() = stream.run {
        val phraseId = readInt()
        val difficulty = readInt()
        val empty = readInt()
        val levelJump = readByte()
        val redundant = readShort()
        val padding = readByte()
        PhraseProperty(phraseId, redundant, levelJump, empty, difficulty)
    }

    private fun readNewLinkedDiff() = stream.run {
        val levelBreak = readInt()
        val phraseCount = readInt()
        val nldPhrase = Array(phraseCount) { readInt() }
        NewLinkedDiff(levelBreak, "", phraseCount, nldPhrase.map { i -> NldPhrase(i) })
    }

    private fun readEvent() = stream.run {
        val time = readFloat()
        val eventName = readString(length = 256).trimEnd('\u0000')
        Event(time, eventName)
    }

    private fun readTone() = stream.run {
        val time = readFloat()
        val toneId = readInt()
        // TODO tone ID handling
        Tone2014(time, toneId, "importedtone_$toneId")
    }

    private fun readSection() = stream.run {
        val name = readString(length = 32).trimEnd('\u0000')
        val number = readInt()
        val startTime = readFloat()
        val endTime = readFloat()
        val startPhraseIterationId = readInt()
        val endPhraseIterationId = readInt()
        val stringBytes = Array(36) { readByte() }
        Section(name, number, startTime)
    }

    private fun readAnchor() = stream.run {
        val startBeatTime = readFloat()
        val endBeatTime = readFloat()
        val unk3FirstNoteTime = readFloat()
        val unk4LastNoteTime = readFloat()
        val fretId = readByte()
        val padding = Array(3) { readByte() }
        val width = readInt()
        val phraseIterationId = readInt()
        Anchor2014(startBeatTime, fretId, width.toShort())
    }

    private fun readAnchorExtension() = stream.run {
        val beatTime = readFloat()
        val fretId = readByte()
        val unk2 = readInt()
        val unk3 = readShort()
        val unk4 = readByte()
        Unit
    }

    private fun readBendValue32() = stream.run {
        val time = readFloat()
        val step = readFloat()
        val unk3 = readShort()
        val unk4 = readByte()
        val unk5 = readByte()
        BendValue(time, step, unk5)
    }

    private fun readBendValue() = stream.run {
        val bv = List(32) { readBendValue32() }
        val usedCount = readInt()
        bv.slice(0 until usedCount)
//        bv.filter { it.time != 0f && it.step != 0f } // is it better?
    }

    private fun readNoteOrChord(
        chordTemplates : List<ChordTemplate2014>,
        cNotes : List<Array<Note2014>>
    ) = stream.run {
        val noteMask = readInt()
        val noteFlags = readInt()
        val hash = readInt()
        val time = readFloat()
        val stringIndex = readByte()
        val fretId = readByte()
        val anchorFretId = readByte()
        val anchorWidth = readByte()
        val chordId = readInt()
        val chordNotesId = readInt()
        val phraseId = readInt()
        val phraseIterationId = readInt()
        val fingerPrintId = Array(2) { readShort() }
        val nextIterNote = readShort()
        val prevIterNote = readShort()
        val parentPrevNote = readShort()
        val slideTo = readByte()
        val slideUnpitchTo = readByte()
        val leftHand = readByte()
        val tap = readByte()
        val pickDirection = readByte()
        val slap = readByte()
        val pluck = readByte()
        val vibrato = readShort()
        val sustain = readFloat()
        val maxBend = readFloat()
        val bendData = readMany { readBendValue32() }
        fun maskValue(flag : Int) : Byte =
            if (noteMask and flag != 0) 1 else 0
        if (chordId == -1)
            Note2014(
                time, maskValue(NOTE_MASK_PARENT), maskValue(NOTE_MASK_CHILD),
                maskValue(NOTE_MASK_ACCENT), maxBend, fretId,
                maskValue(NOTE_MASK_HAMMERON), maskValue(NOTE_MASK_HARMONIC), 0,
                maskValue(NOTE_MASK_IGNORE), leftHand, maskValue(NOTE_MASK_MUTE),
                maskValue(NOTE_MASK_PALMMUTE), pluck, maskValue(NOTE_MASK_PULLOFF), slap, slideTo,
                stringIndex, sustain, maskValue(NOTE_MASK_TREMOLO), maskValue(NOTE_MASK_PINCHHARMONIC),
                pickDirection, maskValue(NOTE_MASK_RIGHTHAND),
                slideUnpitchTo, tap, vibrato, bendData)//.fold(listOf()) { a, b -> a + b })
        else {
            val chordNotes = ArrayList<Note2014>()
            if (chordNotesId != -1 && chordNotesId < cNotes.size) {
                val template = chordTemplates[chordId];
                for (i in cNotes[chordNotesId].indices) {
                    if (template.fret[i].toInt() == -1 || template.finger[i].toInt() == -1)
                        continue // TODO is this right?
                    val note = cNotes[chordNotesId][i]
                    chordNotes.add(Note2014(
                        time, note.linkNext, note.linked, note.accent, note.bend, template.fret[i],
                        note.hammerOn, note.harmonic, note.hopo, note.ignore,
                        template.finger[i], note.mute, note.palmMute, note.pluck, note.pullOff,
                        note.slap, note.slideTo, note.string, sustain, note.tremolo,
                        note.harmonicPinch, note.pickDirection, note.rightHand,
                        note.slideUnpitchTo, note.tap, note.vibrato, note.bendValues
                    ))
                }
            }
            Chord2014(
                time, maskValue(NOTE_MASK_PARENT), maskValue(NOTE_MASK_ACCENT),
                chordId, maskValue(NOTE_MASK_FRETHANDMUTE), maskValue(NOTE_MASK_HIGHDENSITY),
                maskValue(NOTE_MASK_IGNORE), maskValue(NOTE_MASK_PALMMUTE), 0,
                if (maskValue(NOTE_MASK_STRUM) > 0) "up" else "down", chordNotes
            )
        }
    }

    private fun readHandShape() = stream.run {
        val chordId = readInt()
        val startTime = readFloat()
        val endTime = readFloat()
        val unk3FirstNoteTime = readFloat()
        val unk4LastNoteTime = readFloat()
        HandShape(chordId, endTime, startTime)
    }

    private fun readLevel(
        chordTemplates : List<ChordTemplate2014>,
        cNotes : List<Array<Note2014>>
    ) = stream.run {
        val difficulty = readInt()
        val anchors = readMany { readAnchor() }
        val anchorExtensions = readMany { readAnchorExtension() }
        val fingerprints1 = readMany { readHandShape() }
        val fingerprints2 = readMany { readHandShape() }
        val notesAndChords = readMany { readNoteOrChord(chordTemplates, cNotes) }
        val averageNotesPerIteration = readMany { readFloat() }
        val notesInIteration1 = readMany { readInt() }
        val notesInIteration2 = readMany { readInt() }
        val handShapes = fingerprints1 + fingerprints2
        Level2014(difficulty, notesAndChords.filterIsInstance<Note2014>(),
            notesAndChords.filterIsInstance<Chord2014>(), anchors, handShapes)
    }

    private fun readMetadata(song : Song2014) = stream.run {
        val maxScore = readDouble()
        val maxNotesAndChords = readDouble()
        val maxNotesAndChordsReal = readDouble()
        val pointsPerNote = readDouble()
        val firstBeatLength = readFloat()
        val startTime = readFloat()
        val capoFretId = readByte()
        val lastConversionDateTime = readString(length = 32).trimEnd('\u0000')
        val part = readShort()
        val songLength = readFloat()
        val stringCount = readInt()
        val tuning = readMany { readShort() }
        val unk11FirstNoteTime = readFloat()
        val unk12FirstNoteTime = readFloat()
        val maxDifficulty = readInt()
        song.part = part.toInt()
        song.songLength = songLength
        song.capo = if (capoFretId.toInt() == -1) 0 else capoFretId.toInt()
        song.lastConversionDateTime = lastConversionDateTime
        song.tuning = Tuning(tuning)
    }

    private fun readDna() = stream.run {
        val time = readFloat()
        val dnaId = readInt()
        Unit
    }

    private fun readAction() = stream.run {
        val time = readFloat()
        val actionName = readString(length = 256).trimEnd('\u0000')
        Unit
    }

    private fun readVocal() = stream.run {
        val time = readFloat()
        val note = readInt()
        val length = readFloat()
        val lyric = readString(length = 48).trimEnd('\u0000')
        Unit
    }

    private fun readChordNotes() = stream.run {
        val noteMask = Array(6) { readInt() }
        val bendData = Array(6) { readBendValue() }
        val slideTo = Array(6) { readByte() }
        val slideUnpitchTo = Array(6) { readByte() }
        val vibrato = Array(6) { readShort() }
        Array(6) { i ->
            fun maskValue(flag : Int) : Byte =
                if (noteMask[i] and flag != 0) 1 else 0
            val maxBend = bendData[i].map { it.step }.max() ?: 0f
            Note2014(
                0f, maskValue(NOTE_MASK_PARENT), maskValue(NOTE_MASK_CHILD),
                maskValue(NOTE_MASK_ACCENT), maxBend,
                -1, maskValue(NOTE_MASK_HAMMERON), maskValue(NOTE_MASK_HARMONIC), 0,
                maskValue(NOTE_MASK_IGNORE), -1, maskValue(NOTE_MASK_MUTE),
                maskValue(NOTE_MASK_PALMMUTE), -1, maskValue(NOTE_MASK_PULLOFF), -1, slideTo[i],
                i.toByte(), 0f, maskValue(NOTE_MASK_TREMOLO), maskValue(NOTE_MASK_PINCHHARMONIC),
                -1, maskValue(NOTE_MASK_RIGHTHAND),
                slideUnpitchTo[i], 0, vibrato[i], bendData[i]
            )
        }
    }

    private fun readSong2014() = stream.run {
        val song = Song2014()
        song.title = attributes.songName
        song.arrangement = when (attributes.arrangementType) {
            0 -> "Lead"
            1 -> "Rhythm"
            2 -> "Combo"
            3 -> "Bass"
            4 -> "Vocals"
            5 -> "JVocals"
            6 -> "ShowLights"
            else -> ""
        }
        song.offset = attributes.songOffset
        song.centOffset = attributes.centOffset
        song.songNameSort = attributes.songNameSort
        song.averageTempo = attributes.songAverageTempo
        song.artistName = attributes.artistName
        song.artistNameSort = attributes.artistNameSort
        song.albumName = attributes.albumName
        song.albumNameSort = attributes.albumNameSort
        song.albumYear = attributes.songYear
        song.albumArt = attributes.albumArt
        song.persistentID = attributes.persistentID
        song.arrangementProperties = ArrangementProperties2014(attributes.arrangementProperties)
        song.toneA = attributes.toneA
        song.toneB = attributes.toneB
        song.toneC = attributes.toneC
        song.toneD = attributes.toneD
        song.toneBase = attributes.toneBase


        song.ebeats = readMany { readEBeat() }
        song.startBeat = song.ebeats[0].time
        song.phrases = readMany { readPhrase() }
        song.chordTemplates = readMany { readChordTemplate() }
        val cNotes = readMany { readChordNotes() }
        readMany { readVocal() }
        song.phraseIterations = readMany { readPhraseIteration() }
        song.phraseProperties = readMany { readPhraseProperties() }
        song.newLinkedDiffs = readMany { readNewLinkedDiff() }
        readMany { readAction() }
        song.events = readMany { readEvent() }
        song.tones = readMany { readTone() }
        readMany { readDna() }
        song.sections = readMany { readSection() }
        song.levels = readMany { readLevel(song.chordTemplates, cNotes) }
        readMetadata(song)
        song.transcriptionTrack = TranscriptionTrack2014()
        song
    }

    val song = readSong2014()
    
    companion object {
        private const val NOTE_MASK_UNDEFINED         = 0x0;
        private const val NOTE_MASK_CHORD             = 0x02;
        private const val NOTE_MASK_OPEN              = 0x04;
        private const val NOTE_MASK_FRETHANDMUTE      = 0x08;
        private const val NOTE_MASK_TREMOLO           = 0x10;
        private const val NOTE_MASK_HARMONIC          = 0x20;
        private const val NOTE_MASK_PALMMUTE          = 0x40;
        private const val NOTE_MASK_SLAP              = 0x80;
        private const val NOTE_MASK_PLUCK             = 0x0100;
        private const val NOTE_MASK_POP               = 0x0100;
        private const val NOTE_MASK_HAMMERON          = 0x0200;
        private const val NOTE_MASK_PULLOFF           = 0x0400;
        private const val NOTE_MASK_SLIDE             = 0x0800;
        private const val NOTE_MASK_BEND              = 0x1000;
        private const val NOTE_MASK_SUSTAIN           = 0x2000;
        private const val NOTE_MASK_TAP               = 0x4000;
        private const val NOTE_MASK_PINCHHARMONIC     = 0x8000;
        private const val NOTE_MASK_VIBRATO           = 0x010000;
        private const val NOTE_MASK_MUTE              = 0x020000;
        private const val NOTE_MASK_IGNORE            = 0x040000;   // ignore=1
        private const val NOTE_MASK_LEFTHAND          = 0x00080000;
        private const val NOTE_MASK_RIGHTHAND         = 0x00100000;
        private const val NOTE_MASK_HIGHDENSITY       = 0x200000;
        private const val NOTE_MASK_SLIDEUNPITCHEDTO  = 0x400000;
        private const val NOTE_MASK_SINGLE            = 0x00800000; // single note
        private const val NOTE_MASK_CHORDNOTES        = 0x01000000; // has chordnotes exported
        private const val NOTE_MASK_DOUBLESTOP        = 0x02000000;
        private const val NOTE_MASK_ACCENT            = 0x04000000;
        private const val NOTE_MASK_PARENT            = 0x08000000; // linkNext=1
        private const val NOTE_MASK_CHILD             = 0x10000000; // note after linkNext=1
        private const val NOTE_MASK_ARPEGGIO          = 0x20000000;
        // missing - not used in lessons/songs            0x40000000
        private const val NOTE_MASK_STRUM             = 0x80000000.toInt(); // handShape defined at chord time

        private const val NOTE_MASK_ARTICULATIONS_RH  = 0x0000C1C0;
        private const val NOTE_MASK_ARTICULATIONS_LH  = 0x00020628;
        private const val NOTE_MASK_ARTICULATIONS     = 0x0002FFF8;
        private const val NOTE_MASK_ROTATION_DISABLED = 0x0000C1E0;
    }
}