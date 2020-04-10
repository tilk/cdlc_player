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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class Chord2014() {
    constructor(
        time : Float,
        linkNext : Byte,
        accent : Byte,
        chordId : Int,
        fretHandMute : Byte,
        highDensity : Byte,
        ignore : Byte,
        palmMute : Byte,
        hopo : Int,
        strum : String,
        chordNotes : List<Note2014>
    ) : this() {
        this.time = time
        this.linkNext = linkNext
        this.accent = accent
        this.chordId = chordId
        this.fretHandMute = fretHandMute
        this.highDensity = highDensity
        this.ignore = ignore
        this.palmMute = palmMute
        this.hopo = hopo
        this.strum = strum
        this.chordNotes = chordNotes
    }
    @JacksonXmlProperty(isAttribute = true, localName = "time")
    var time : Float = 0f
    @JacksonXmlProperty(isAttribute = true, localName = "linkNext")
    var linkNext : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "accent")
    var accent : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "chordId")
    var chordId : Int = -1
    @JacksonXmlProperty(isAttribute = true, localName = "fretHandMute")
    var fretHandMute : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "highDensity")
    var highDensity : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "ignore")
    var ignore : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "palmMute")
    var palmMute : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "hopo")
    var hopo : Int = -1
    @JacksonXmlProperty(isAttribute = true, localName = "strum")
    var strum : String = ""
    @JacksonXmlProperty(localName = "chordNote")
    @JacksonXmlElementWrapper(useWrapping = false)
    var chordNotes : List<Note2014> = ArrayList()
}
