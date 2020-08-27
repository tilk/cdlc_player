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

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class Level2014() {
    constructor(
        difficulty : Int,
        notes : List<Note2014>,
        chords : List<Chord2014>,
        anchors : List<Anchor2014>,
        handShapes : List<HandShape>
    ) : this() {
        this.difficulty = difficulty
        this.notes = notes
        this.chords = chords
        this.anchors = anchors
        this.handShapes = handShapes
    }
    @JacksonXmlProperty(isAttribute = true, localName = "difficulty")
    var difficulty: Int = -1
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "note")
    @JacksonXmlElementWrapper(localName = "notes")
    var notes: List<Note2014> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "chord")
    @JacksonXmlElementWrapper(localName = "chords")
    var chords: List<Chord2014> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "anchor")
    @JacksonXmlElementWrapper(localName = "anchors")
    var anchors: List<Anchor2014> = ArrayList()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "handShape")
    @JacksonXmlElementWrapper(localName = "handShapes")
    var handShapes: List<HandShape> = ArrayList()
}