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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class ArrangementProperties2014(
    @JacksonXmlProperty(isAttribute = true, localName = "bonusArr")
    var bonusArr : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "Metronome")
    var metronome : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "pathLead")
    var pathLead : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "pathRhythm")
    var pathRhythm : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "pathBass")
    var pathBass : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "routeMask")
    var routeMask : Int
)
