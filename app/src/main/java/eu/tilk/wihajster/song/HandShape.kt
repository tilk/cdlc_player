package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class HandShape(
    @JacksonXmlProperty(isAttribute = true)
    val chordId : Int,
    @JacksonXmlProperty(isAttribute = true)
    val endTime : Float,
    @JacksonXmlProperty(isAttribute = true)
    val startTime : Float
)