package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class Chord2014() {
    constructor(
        time : Float,
        linkNext : Byte,
        accent : Byte,
        chordId : Int,
        fretHandMute : Byte,
        highDensity : Byte,
        ignore : Byte,
        palmMute : Byte,
        hopo : Int,
        strum : String,
        chordNotes : List<Note2014>
    ) : this() {
        this.time = time
        this.linkNext = linkNext
        this.accent = accent
        this.chordId = chordId
        this.fretHandMute = fretHandMute
        this.highDensity = highDensity
        this.ignore = ignore
        this.palmMute = palmMute
        this.hopo = hopo
        this.strum = strum
        this.chordNotes = chordNotes
    }
    @JacksonXmlProperty(isAttribute = true, localName = "time")
    var time : Float = 0f
    @JacksonXmlProperty(isAttribute = true, localName = "linkNext")
    var linkNext : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "accent")
    var accent : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "chordId")
    var chordId : Int = -1
    @JacksonXmlProperty(isAttribute = true, localName = "fretHandMute")
    var fretHandMute : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "highDensity")
    var highDensity : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "ignore")
    var ignore : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "palmMute")
    var palmMute : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "hopo")
    var hopo : Int = -1
    @JacksonXmlProperty(isAttribute = true, localName = "strum")
    var strum : String = ""
    @JacksonXmlProperty(localName = "chordNote")
    @JacksonXmlElementWrapper(useWrapping = false)
    var chordNotes : List<Note2014> = ArrayList()
}
