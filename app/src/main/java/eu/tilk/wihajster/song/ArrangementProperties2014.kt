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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class ArrangementProperties2014(
    @JsonProperty("bonusArr")
    @JacksonXmlProperty(isAttribute = true, localName = "bonusArr")
    var bonusArr : Int,
    @JsonProperty("Metronome")
    @JacksonXmlProperty(isAttribute = true, localName = "Metronome")
    var metronome : Int,
    @JsonProperty("pathLead")
    @JacksonXmlProperty(isAttribute = true, localName = "pathLead")
    var pathLead : Int,
    @JsonProperty("pathRhythm")
    @JacksonXmlProperty(isAttribute = true, localName = "pathRhythm")
    var pathRhythm : Int,
    @JsonProperty("pathBass")
    @JacksonXmlProperty(isAttribute = true, localName = "pathBass")
    var pathBass : Int,
    @JsonProperty("routeMask")
    @JacksonXmlProperty(isAttribute = true, localName = "routeMask")
    var routeMask : Int,
    @JsonProperty("represent")
    @JacksonXmlProperty(isAttribute = true, localName = "represent")
    var represent : Int,
    @JsonProperty("standardTuning")
    @JacksonXmlProperty(isAttribute = true, localName = "standardTuning")
    var standardTuning : Int,
    @JsonProperty("nonStandardChords")
    @JacksonXmlProperty(isAttribute = true, localName = "nonStandardChords")
    var nonStandardChords : Int,
    @JsonProperty("barreChords")
    @JacksonXmlProperty(isAttribute = true, localName = "barreChords")
    var barreChords : Int,
    @JsonProperty("powerChords")
    @JacksonXmlProperty(isAttribute = true, localName = "powerChords")
    var powerChords : Int,
    @JsonProperty("dropDPower")
    @JacksonXmlProperty(isAttribute = true, localName = "dropDPower")
    var dropDPower : Int,
    @JsonProperty("openChords")
    @JacksonXmlProperty(isAttribute = true, localName = "openChords")
    var openChords : Int,
    @JsonProperty("fingerPicking")
    @JacksonXmlProperty(isAttribute = true, localName = "fingerPicking")
    var fingerPicking : Int,
    @JsonProperty("pickDirection")
    @JacksonXmlProperty(isAttribute = true, localName = "pickDirection")
    var pickDirection : Int,
    @JsonProperty("doubleStops")
    @JacksonXmlProperty(isAttribute = true, localName = "doubleStops")
    var doubleStops : Int,
    @JsonProperty("palmMutes")
    @JacksonXmlProperty(isAttribute = true, localName = "palmMutes")
    var palmMutes : Int,
    @JsonProperty("harmonics")
    @JacksonXmlProperty(isAttribute = true, localName = "harmonics")
    var harmonics : Int,
    @JsonProperty("pinchHarmonics")
    @JacksonXmlProperty(isAttribute = true, localName = "pinchHarmonics")
    var pinchHarmonics : Int,
    @JsonProperty("hopo")
    @JacksonXmlProperty(isAttribute = true, localName = "hopo")
    var hopo : Int,
    @JsonProperty("tremolo")
    @JacksonXmlProperty(isAttribute = true, localName = "tremolo")
    var tremolo : Int,
    @JsonProperty("slides")
    @JacksonXmlProperty(isAttribute = true, localName = "slides")
    var slides : Int,
    @JsonProperty("unpitchedSlides")
    @JacksonXmlProperty(isAttribute = true, localName = "unpitchedSlides")
    var unpitchedSlides : Int,
    @JsonProperty("bends")
    @JacksonXmlProperty(isAttribute = true, localName = "bends")
    var bends : Int,
    @JsonProperty("tapping")
    @JacksonXmlProperty(isAttribute = true, localName = "tapping")
    var tapping : Int,
    @JsonProperty("vibrato")
    @JacksonXmlProperty(isAttribute = true, localName = "vibrato")
    var vibrato : Int,
    @JsonProperty("fretHandMutes")
    @JacksonXmlProperty(isAttribute = true, localName = "fretHandMutes")
    var fretHandMutes : Int,
    @JsonProperty("slapPop")
    @JacksonXmlProperty(isAttribute = true, localName = "slapPop")
    var slapPop : Int,
    @JsonProperty("twoFingerPicking")
    @JacksonXmlProperty(isAttribute = true, localName = "twoFingerPicking")
    var twoFingerPicking : Int,
    @JsonProperty("fifthsAndOctaves")
    @JacksonXmlProperty(isAttribute = true, localName = "fifthsAndOctaves")
    var fifthsAndOctaves : Int,
    @JsonProperty("syncopation")
    @JacksonXmlProperty(isAttribute = true, localName = "syncopation")
    var syncopation : Int,
    @JsonProperty("bassPick")
    @JacksonXmlProperty(isAttribute = true, localName = "bassPick")
    var bassPick : Int,
    @JsonProperty("sustain")
    @JacksonXmlProperty(isAttribute = true, localName = "sustain")
    var sustain : Int
) {
    constructor(m : Map<String, Int>) : this(
        m.getOrElse("bonusArr") { 0 },
        m.getOrElse("Metronome") { 0 },
        m.getOrElse("pathLead") { 0 },
        m.getOrElse("pathRhythm") { 0 },
        m.getOrElse("pathBass") { 0 },
        m.getOrElse("routeMask") { 0 },
        m.getOrElse("represent") { 0 },
        m.getOrElse("standardTuning") { 0 },
        m.getOrElse("nonStandardChords") { 0 },
        m.getOrElse("barreChords") { 0 },
        m.getOrElse("powerChords") { 0 },
        m.getOrElse("dropDPower") { 0 },
        m.getOrElse("openChords") { 0 },
        m.getOrElse("fingerPicking") { 0 },
        m.getOrElse("pickDirection") { 0 },
        m.getOrElse("doubleStops") { 0 },
        m.getOrElse("palmMutes") { 0 },
        m.getOrElse("harmonics") { 0 },
        m.getOrElse("pinchHarmonics") { 0 },
        m.getOrElse("hopo") { 0 },
        m.getOrElse("tremolo") { 0 },
        m.getOrElse("slides") { 0 },
        m.getOrElse("unpitchedSlides") { 0 },
        m.getOrElse("bends") { 0 },
        m.getOrElse("tapping") { 0 },
        m.getOrElse("vibrato") { 0 },
        m.getOrElse("fretHandMutes") { 0 },
        m.getOrElse("slapPop") { 0 },
        m.getOrElse("twoFingerPicking") { 0 },
        m.getOrElse("fifthsAndOctaves") { 0 },
        m.getOrElse("syncopation") { 0 },
        m.getOrElse("bassPick") { 0 },
        m.getOrElse("sustain") { 0 }
    )
}