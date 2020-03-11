package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Chord2014(
    @JacksonXmlProperty(isAttribute = true)
    val time : Float,
    @JacksonXmlProperty(isAttribute = true)
    val linkNext : Int,
    @JacksonXmlProperty(isAttribute = true)
    val accent : Int,
    @JacksonXmlProperty(isAttribute = true)
    val chordId : Int,
    @JacksonXmlProperty(isAttribute = true)
    val fretHandMute : Int,
    @JacksonXmlProperty(isAttribute = true)
    val highDensity : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val ignore : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val palmMute : Int,
    @JacksonXmlProperty(isAttribute = true)
    val hopo : Int,
    @JacksonXmlProperty(isAttribute = true)
    val strum : String,
    @JacksonXmlProperty(localName = "chordNote")
    @JacksonXmlElementWrapper(useWrapping = false)
    val chordNotes : List<Note2014>
)
