package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class BendValue(
    @JacksonXmlProperty(isAttribute = true, localName = "time")
    var time : Float,
    @JacksonXmlProperty(isAttribute = true, localName = "step")
    var step : Float,
    @JacksonXmlProperty(isAttribute = true, localName = "unk5")
    var unk5 : Byte
)