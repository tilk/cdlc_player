package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class Chord2014 {
    @JacksonXmlProperty(isAttribute = true, localName = "time")
    var time : Float = 0f
    @JacksonXmlProperty(isAttribute = true, localName = "linkNext")
    var linkNext : Int = -1
    @JacksonXmlProperty(isAttribute = true, localName = "accent")
    var accent : Int = -1
    @JacksonXmlProperty(isAttribute = true, localName = "chordId")
    var chordId : Int = -1
    @JacksonXmlProperty(isAttribute = true, localName = "fretHandMute")
    var fretHandMute : Int = -1
    @JacksonXmlProperty(isAttribute = true, localName = "highDensity")
    var highDensity : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "ignore")
    var ignore : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "palmMute")
    var palmMute : Int = -1
    @JacksonXmlProperty(isAttribute = true, localName = "hopo")
    var hopo : Int = -1
    @JacksonXmlProperty(isAttribute = true, localName = "strum")
    var strum : String = ""
    @JacksonXmlProperty(localName = "chordNote")
    @JacksonXmlElementWrapper(useWrapping = false)
    var chordNotes : List<Note2014> = ArrayList()
}
