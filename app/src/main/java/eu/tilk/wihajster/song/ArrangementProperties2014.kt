package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class ArrangementProperties2014(
    @JacksonXmlProperty(isAttribute = true)
    val bonusArr : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "Metronome")
    val metronome : Int,
    @JacksonXmlProperty(isAttribute = true)
    val pathLead : Int,
    @JacksonXmlProperty(isAttribute = true)
    val pathRhythm : Int,
    @JacksonXmlProperty(isAttribute = true)
    val pathBass : Int,
    @JacksonXmlProperty(isAttribute = true)
    val routeMask : Int
)
