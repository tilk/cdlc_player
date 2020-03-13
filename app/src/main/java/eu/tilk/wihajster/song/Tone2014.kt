package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Tone2014(
    @JacksonXmlProperty(isAttribute = true, localName = "time")
    var time : Float,
    @JacksonXmlProperty(isAttribute = true, localName = "id")
    var id : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    var name : String
)
