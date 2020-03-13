package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class HandShape(
    @JacksonXmlProperty(isAttribute = true, localName = "chordId")
    var chordId : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "endTime")
    var endTime : Float,
    @JacksonXmlProperty(isAttribute = true, localName = "startTime")
    var startTime : Float
)