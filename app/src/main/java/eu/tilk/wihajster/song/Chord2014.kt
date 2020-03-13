package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Chord2014(
    @JacksonXmlProperty(isAttribute = true, localName = "time")
    var time : Float,
    @JacksonXmlProperty(isAttribute = true, localName = "linkNext")
    var linkNext : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "accent")
    var accent : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "chordId")
    var chordId : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "fretHandMute")
    var fretHandMute : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "highDensity")
    var highDensity : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "ignore")
    var ignore : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "palmMute")
    var palmMute : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "hopo")
    var hopo : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "strum")
    var strum : String/* TODO,
    @JacksonXmlProperty(localName = "chordNote")
    @JacksonXmlElementWrapper(useWrapping = false)
    var chordNotes : List<Note2014> = ArrayList()*/
)
