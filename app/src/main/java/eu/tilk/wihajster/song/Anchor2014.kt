package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Anchor2014(
    @JacksonXmlProperty(isAttribute = true, localName = "time")
    var time : Float = 0f,
    @JacksonXmlProperty(isAttribute = true, localName = "fret")
    var fret : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "width")
    var width : Short = -1
)
