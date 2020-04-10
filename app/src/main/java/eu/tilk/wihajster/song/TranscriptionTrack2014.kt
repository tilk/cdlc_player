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

class TranscriptionTrack2014 {
    @JacksonXmlProperty(isAttribute = true, localName = "difficulty")
    var difficulty: Int = -1
    @JacksonXmlProperty(localName = "note")
    @JacksonXmlElementWrapper(localName = "notes")
    var notes: List<Note2014> = ArrayList()
    @JacksonXmlProperty(localName = "chord")
    @JacksonXmlElementWrapper(localName = "chords")
    var chords: List<Chord2014> = ArrayList()
    @JacksonXmlProperty(localName = "anchor")
    @JacksonXmlElementWrapper(localName = "anchors")
    var anchors: List<Anchor2014> = ArrayList()
    @JacksonXmlProperty(localName = "handShape")
    @JacksonXmlElementWrapper(localName = "handShapes")
    var handShapes: List<HandShape> = ArrayList()
}
