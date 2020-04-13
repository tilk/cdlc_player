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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class ChordTemplate2014(
    @JacksonXmlProperty(isAttribute = true, localName = "displayName")
    var displayName : String,
    @JacksonXmlProperty(isAttribute = true, localName = "chordName")
    var chordName : String,
    @JacksonXmlProperty(isAttribute = true, localName = "fret0")
    var fret0 : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "fret1")
    var fret1 : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "fret2")
    var fret2 : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "fret3")
    var fret3 : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "fret4")
    var fret4 : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "fret5")
    var fret5 : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger0")
    var finger0 : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger1")
    var finger1 : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger2")
    var finger2 : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger3")
    var finger3 : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger4")
    var finger4 : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger5")
    var finger5 : Byte = -1
) {
    val fret : Array<Byte>
        @JsonIgnore get() = arrayOf(fret0, fret1, fret2, fret3, fret4, fret5)
    val finger : Array<Byte>
        @JsonIgnore get() = arrayOf(finger0, finger1, finger2, finger3, finger4, finger5)
}
