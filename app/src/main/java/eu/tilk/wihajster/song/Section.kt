package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Section(
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    var name : String,
    @JacksonXmlProperty(isAttribute = true, localName = "number")
    var number : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "startTime")
    var startTime : Float
)
