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

sealed class Event {
    abstract val time : Float

    data class Note(
        override val time : Float,
        val fret : Byte,
        val string : Byte,
        val sustain : Float = 0f,
        val slideTo : Byte = -1,
        val slideUnpitchedTo : Byte = -1,
        val tremolo : Boolean = false,
        val linked : Boolean = false,
        val vibrato : Short = 0,
        val bend : List<Pair<Float, Float>> = ArrayList()
    ) : Event()

    data class Beat(
        override val time : Float,
        val measure : Short
    ) : Event()

    data class Anchor(
        override val time : Float,
        val fret : Byte,
        val width : Short
    ) : Event()

    data class Chord(
        override val time : Float,
        val id : Int
    ) : Event()
}