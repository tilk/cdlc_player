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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Tuning(
    @JacksonXmlProperty(isAttribute = true, localName = "string0")
    var string0 : Short,
    @JacksonXmlProperty(isAttribute = true, localName = "string1")
    var string1 : Short,
    @JacksonXmlProperty(isAttribute = true, localName = "string2")
    var string2 : Short,
    @JacksonXmlProperty(isAttribute = true, localName = "string3")
    var string3 : Short,
    @JacksonXmlProperty(isAttribute = true, localName = "string4")
    var string4 : Short,
    @JacksonXmlProperty(isAttribute = true, localName = "string5")
    var string5 : Short
) {
    constructor(strings : List<Short>) : this(
        (strings.getOrNull(0) ?: -1),
        (strings.getOrNull(1) ?: -1),
        (strings.getOrNull(2) ?: -1),
        (strings.getOrNull(3) ?: -1),
        (strings.getOrNull(4) ?: -1),
        (strings.getOrNull(5) ?: -1)
    ){}
    fun name() : String = when {
        strings.all { it == string0 } -> "Standard " + stringName(string0)
        strings.subList(1, 5).all { it == (string0 - 1).toShort() } -> "Drop " + stringName(string0)
        else -> "Custom"
    }
    val strings : List<Short>
        get() = arrayListOf(string0, string1, string2, string3, string4, string5)
    companion object {
        private fun stringName(s : Short) = when(s.toInt()) {
            0 -> "E"
            1 -> "D♯"
            2 -> "D"
            3 -> "C♯"
            4 -> "C"
            5 -> "B"
            else -> "?"
        }
    }
}
