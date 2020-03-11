package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class ChordTemplate2014(
    @JacksonXmlProperty(isAttribute = true)
    val displayName : String,
    @JacksonXmlProperty(isAttribute = true)
    val chordName : String,
    @JacksonXmlProperty(isAttribute = true)
    val fret0 : Int = -1,
    @JacksonXmlProperty(isAttribute = true)
    val fret1 : Int = -1,
    @JacksonXmlProperty(isAttribute = true)
    val fret2 : Int = -1,
    @JacksonXmlProperty(isAttribute = true)
    val fret3 : Int = -1,
    @JacksonXmlProperty(isAttribute = true)
    val fret4 : Int = -1,
    @JacksonXmlProperty(isAttribute = true)
    val fret5 : Int = -1,
    @JacksonXmlProperty(isAttribute = true)
    val finger0 : Int = -1,
    @JacksonXmlProperty(isAttribute = true)
    val finger1 : Int = -1,
    @JacksonXmlProperty(isAttribute = true)
    val finger2 : Int = -1,
    @JacksonXmlProperty(isAttribute = true)
    val finger3 : Int = -1,
    @JacksonXmlProperty(isAttribute = true)
    val finger4 : Int = -1,
    @JacksonXmlProperty(isAttribute = true)
    val finger5 : Int = -1
)
