package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Control(
    @JacksonXmlProperty(isAttribute = true)
    val time : Float,
    @JacksonXmlProperty(isAttribute = true)
    val code : String
)

