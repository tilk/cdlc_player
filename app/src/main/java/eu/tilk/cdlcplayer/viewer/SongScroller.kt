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

package eu.tilk.cdlcplayer.viewer

import androidx.lifecycle.LiveData
import eu.tilk.cdlcplayer.shapes.*
import eu.tilk.cdlcplayer.shapes.utils.NoteCalculator

class SongScroller(
    private val song : List<Event>,
    zHorizon : Float,
    private val calculator : NoteCalculator, // TODO I don't want that stuff here
    private val repeaterInfo : LiveData<RepeaterInfo>,
    private val scrollSpeed : Float // TODO I don't really want scrollSpeed here
) {
    private val songByEndTime = (1 until song.count()).toList().sortedBy { song[it].endTime }
    private val horizon = zHorizon / scrollSpeed
    private var time : Float = 0F
    private var position : Int = 0
    private var events : MutableList<EventShape<Event>> = mutableListOf()
    private val guardAnchor = Event.Anchor(0f, 1, 4, 0f)
    private var lastAnchor : Event.Anchor = guardAnchor
    private val lastAnchorCell = object : Cell<Event.Anchor> {
        override var data : Event.Anchor by ::lastAnchor
    }

    val activeEvents : List<EventShape<Event>> get() = events
    val currentTime : Float get() = time

    private interface Cell<T> {
        var data : T
    }

    private fun shapesForEvent(event : Event, lastAnchor : Cell<Event.Anchor>) = sequence {
        when(event) {
            is Event.Chord -> {
                for (string in -1..if (event.repeated) 2 else 6)
                    yield(Chord(event, lastAnchor.data, string, event.repeated))
                if (!event.repeated)
                    yield(ChordInfo(event, lastAnchor.data))
            }
            is Event.Anchor -> {
                lastAnchor.data = event
                yield(Anchor(event))
            }
            is Event.Note -> {
                if (!event.derived) {
                    for (string in -1 until calculator.sort(event.string))
                        if (event.fret > 0)
                            yield(NoteLocator(event, string))
                        else
                            yield(EmptyStringNoteLocator(event, string, lastAnchor.data))
                    yield(NoteMarker(event, lastAnchor.data))
                }
                if (event.fret > 0)
                    yield(Note(event))
                else
                    yield(EmptyStringNote(event, lastAnchor.data))
            }
            is Event.NoteSustain -> yield(NoteTail(event, lastAnchor.data, scrollSpeed))
            is Event.Beat -> yield(Beat(event, lastAnchor.data, repeaterInfo))
            is Event.HandShape -> yield(ChordSustain(event, lastAnchor.data))
        }
    }

    fun scroll(t: Float) {
        if (t >= 0) advance(t)
        else rewind(-t)
    }

    private fun rewind(t: Float) {
        val prevTime = time
        time -= t

        events.removeAll { it.event.time >= time + horizon }

        val firstAnchor = events.asSequence()
            .map { it.event }
            .find { it is Event.Anchor } as Event.Anchor?

        val cell = object : Cell<Event.Anchor> {
            override var data : Event.Anchor = firstAnchor ?: guardAnchor
        }

        fun List<Int>.binarySearchFirst(comp : (Int) -> Boolean) : Int {
            var l = 0
            var r = this.count()
            while (l < r) {
                val m = (l + r) / 2
                if (comp(this[m])) r = m
                else l = m + 1
            }
            return l
        }

        val startIdx = songByEndTime.binarySearchFirst { song[it].endTime >= time }
        val endIdx = songByEndTime.binarySearchFirst { song[it].endTime >= prevTime }

        val newPre = songByEndTime.subList(startIdx, endIdx)
            .filter { song[it].time < time + horizon }
            .sorted()
            .map { song[it] }

        val new = (if (firstAnchor == null) newPre else listOf(firstAnchor) + newPre)
            .sortedBy { it.time }
            .flatMap { if (it == firstAnchor) { cell.data = firstAnchor; sequenceOf() } else shapesForEvent(it, cell) }

        events.addAll(0, new)

        while (position > 0 && song[position-1].time >= time + horizon)
            position--

        lastAnchor = events.asReversed().asSequence().map { it.event }
            .find { it is Event.Anchor } as Event.Anchor? ?: guardAnchor
    }

    fun advance(t : Float, onRemove : (Event, Boolean) -> Unit = { _ , _ -> }) {
        time += t

        val it = events.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e.endTime < time) {
                onRemove(e.event, e.derived)
                it.remove()
            }
        }

        while (position < song.size && song[position].time < time + horizon) {
            events.addAll(shapesForEvent(song[position++], lastAnchorCell))
        }
    }

    fun repeat() {
        val repeat = repeaterInfo.value
        if (repeat != null) {
            if (time >= repeat.endBeat.time)
                rewind(time - repeat.startBeat.time + repeat.beatPeriod)
        }
    }
}