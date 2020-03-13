package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class ChordTemplate2014(
    @JacksonXmlProperty(isAttribute = true, localName = "displayName")
    var displayName : String,
    @JacksonXmlProperty(isAttribute = true, localName = "chordName")
    var chordName : String,
    @JacksonXmlProperty(isAttribute = true, localName = "fret0")
    var fret0 : Int = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "fret1")
    var fret1 : Int = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "fret2")
    var fret2 : Int = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "fret3")
    var fret3 : Int = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "fret4")
    var fret4 : Int = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "fret5")
    var fret5 : Int = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger0")
    var finger0 : Int = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger1")
    var finger1 : Int = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger2")
    var finger2 : Int = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger3")
    var finger3 : Int = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger4")
    var finger4 : Int = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "finger5")
    var finger5 : Int = -1
)
