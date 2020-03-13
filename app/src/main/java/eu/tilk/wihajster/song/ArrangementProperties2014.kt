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
