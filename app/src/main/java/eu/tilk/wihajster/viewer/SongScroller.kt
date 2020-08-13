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

package eu.tilk.wihajster.viewer

import eu.tilk.wihajster.shapes.*

class SongScroller(
    private val song : List<Event>,
    zHorizon : Float,
    private val scrollSpeed : Float // TODO I don't really want scrollSpeed here
) {
    private val horizon = zHorizon / scrollSpeed
    private var time : Float = 0F
    private var position : Int = 0
    private var lastAnchor =
        Anchor(
            Event.Anchor(
                0f,
                1,
                4
            ), horizon
        )
    private var events : ArrayList<EventShape<Event>> = arrayListOf(lastAnchor)

    val activeEvents : List<EventShape<Event>> get() = events
    val currentTime : Float get() = time

    fun advance(t : Float, onRemove : (Event) -> Unit) {
        time += t

        lastAnchor.lastAnchorTime = time + horizon

        val it = events.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e.endTime < time) {
                onRemove(e.event)
                it.remove()
            }
        }

        fun addNote(event : Event.Note) {
            if (!event.linked)
                if (event.fret > 0)
                    events.add(Note(event))
                else
                    events.add(
                        EmptyStringNote(
                            event,
                            lastAnchor.event
                        )
                    )
            if (event.sustain > 0f)
                events.add(
                    NoteTail(
                        event,
                        lastAnchor.event,
                        scrollSpeed
                    )
                )
        }

        while (position < song.size && song[position].time < time + horizon) {
            when (val event = song[position++]) {
                is Event.Chord -> {
                    events.add(Chord(event, lastAnchor.event))
                    if (!event.repeated) {
                        events.add(ChordInfo(event, lastAnchor.event))
                        for (note in event.notes)
                            addNote(note)
                    }
                }
                is Event.Anchor -> {
                    lastAnchor.lastAnchorTime = event.time
                    lastAnchor = Anchor(event, time + horizon)
                    events.add(lastAnchor)
                }
                is Event.Note -> addNote(event)
                is Event.Beat ->
                    events.add(Beat(event, lastAnchor.event))
            }
        }
    }
}