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

import eu.tilk.cdlcplayer.shapes.logistic

sealed class Event {
    abstract val time : Float
    open val endTime : Float get() = time

    data class Note(
        override val time : Float,
        val fret : Byte,
        val string : Byte,
        val leftHand : Byte = -1,
        val derived : Boolean = false,
        val bend : Float = 0f,
        val effect : Effect? = null
    ) : Event() {
        val fingerInfo get() = if (leftHand in 1..4) FingerInfo(leftHand, string, fret) else null
    }

    data class NoteSustain(
        override val time : Float,
        val fret : Byte,
        val string : Byte,
        val leftHand : Byte = -1,
        val sustain : Float = 0f,
        val slideTo : Byte = -1,
        val slideUnpitchedTo : Byte = -1,
        val tremolo : Boolean = false,
        val linked : Boolean = false,
        val vibrato : Short = 0,
        val bend : List<Pair<Float, Float>> = ArrayList()
    ) : Event() {
        override val endTime : Float get() = time + sustain
        val slideLen = when {
            slideTo > 0 -> slideTo - fret
            slideUnpitchedTo >= 0 -> slideUnpitchedTo - fret
            else -> 0
        }
        fun slideValue(pct : Float) = slideLen * logistic(pct * 10f - 5f)
        private fun findBend(pct : Float) : Pair<Pair<Float, Float>, Pair<Float, Float>> {
            if (bend.isEmpty())
                return Pair(Pair(0f, 0f), Pair(1f, 0f))
            val bi = bend.indexOfFirst { p -> p.first <= pct }
            val start = if (bi == -1) Pair(0f, 0f) else bend[bi]
            val end = if (bend.lastIndex < bi+1)
                Pair(1f, bend.last().second) else bend[bi+1]
            return Pair(start, end)
        }
        fun bendValue(pct : Float) : Float {
            val (start, end) = findBend(pct)
            val prog = (pct - start.first)/(end.first - start.first)
            val amnt = start.second + logistic(prog * 10f - 5f) *
                    (end.second - start.second)
            val dir = if (string > 2) -1f else 1f
            return amnt * dir
        }
    }

    data class Beat(
        override val time : Float,
        val measure : Short
    ) : Event()

    data class Anchor(
        override val time : Float,
        val fret : Byte,
        val width : Short,
        val nextTime : Float = time
    ) : Event() {
        override val endTime : Float get() = nextTime
    }

    data class Chord(
        override val time : Float,
        val id : Int,
        val fingers : List<FingerInfo>,
        val repeated : Boolean,
        val effect : Effect? = null
    ) : Event()

    data class HandShape(
        override val time : Float,
        val sustain : Float,
        val id : Int,
        val fingers : List<FingerInfo>
    ) : Event() {
        override val endTime : Float get() = time + sustain
    }
}