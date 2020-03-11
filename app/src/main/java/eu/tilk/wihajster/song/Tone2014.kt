package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Tone2014(
    @JacksonXmlProperty(isAttribute = true)
    val time : Float,
    @JacksonXmlProperty(isAttribute = true)
    val id : Int,
    @JacksonXmlProperty(isAttribute = true)
    val name : String
)
