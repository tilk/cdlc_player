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

sealed class SortLevel {
    abstract val level : Int

    abstract class Const(c_level : Int) : SortLevel() {
        override val level = c_level
    }

    object Tab : Const(-2)
    object Beat : Const(-1)
    data class ChordBox(val string : Int) : SortLevel() {
        override val level = 3*string+1
    }
    data class String(val string : Int) : SortLevel() {
        override val level = 3*string+2
    }
    data class StringTail(val string : Int) : SortLevel() {
        override val level = 3*string
    }
    object Chord : Const(13)
}