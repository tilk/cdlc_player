package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Phrase(
    @JacksonXmlProperty(isAttribute = true, localName = "disparity")
    var disparity : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "ignore")
    var ignore : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "maxDifficulty")
    var maxDifficulty : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    var name : String,
    @JacksonXmlProperty(isAttribute = true, localName = "solo")
    var solo : Byte
)