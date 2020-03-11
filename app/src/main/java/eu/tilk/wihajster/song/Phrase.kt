package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Phrase(
    @JacksonXmlProperty(isAttribute = true)
    val disparity : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val ignore : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val maxDifficulty : Int,
    @JacksonXmlProperty(isAttribute = true)
    val name : String,
    @JacksonXmlProperty(isAttribute = true)
    val solo : Byte
)