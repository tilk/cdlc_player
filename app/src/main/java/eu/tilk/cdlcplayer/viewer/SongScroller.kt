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

import eu.tilk.cdlcplayer.shapes.*
import eu.tilk.cdlcplayer.shapes.utils.NoteCalculator

class SongScroller(
    private val song : List<Event>,
    zHorizon : Float,
    private val calculator : NoteCalculator,
    private val scrollSpeed : Float // TODO I don't really want scrollSpeed here
) {
    private val horizon = zHorizon / scrollSpeed
    private var time : Float = 0F
    private var position : Int = 0
    private var events : MutableList<EventShape<Event>> = mutableListOf()
    private var lastAnchor : Event.Anchor = Event.Anchor(0f, 1, 4, 0f)
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
                if (!event.derived)
                    for (string in -1 until calculator.sort(event.string))
                        if (event.fret > 0) // TODO empty string locator
                            yield(NoteLocator(event, string))
                if (event.fret > 0)
                    yield(Note(event))
                else
                    yield(EmptyStringNote(event, lastAnchor.data))
            }
            is Event.NoteSustain -> yield(NoteTail(event, lastAnchor.data, scrollSpeed))
            is Event.Beat -> yield(Beat(event, lastAnchor.data))
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

        events.removeAll { e -> e.event.time >= time + horizon }

        val firstAnchor = events.asSequence()
            .map { it.event }
            .find { it is Event.Anchor } as Event.Anchor?

        val cell = object : Cell<Event.Anchor> {
            override var data : Event.Anchor =
                firstAnchor ?: Event.Anchor(0f, 1, 4, 0f)
        }

        // TODO: inefficient
        val new = song
            .filter { e -> e.endTime < prevTime && e.endTime >= time || e == firstAnchor }
            .flatMap { e -> if (e == firstAnchor) { cell.data = firstAnchor; sequenceOf() } else shapesForEvent(e, cell) }

        events.addAll(0, new)

        while (position > 0 && song[position-1].time >= time + horizon)
            position--
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
}