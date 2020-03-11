package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Section(
    @JacksonXmlProperty(isAttribute = true)
    val name : String,
    @JacksonXmlProperty(isAttribute = true)
    val number : Int,
    @JacksonXmlProperty(isAttribute = true)
    val startTime : Float
)
