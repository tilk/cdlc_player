package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Note2014(
    @JacksonXmlProperty(isAttribute = true)
    val time : Float,
    @JacksonXmlProperty(isAttribute = true)
    val linkNext : Int,
    @JacksonXmlProperty(isAttribute = true)
    val accent : Int,
    @JacksonXmlProperty(isAttribute = true)
    val bend : Float,
    @JacksonXmlProperty(isAttribute = true)
    val fret : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val hammerOn : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val harmonic : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val hopo : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val ignore : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val leftHand : Byte = -1,
    @JacksonXmlProperty(isAttribute = true)
    val mute : Int,
    @JacksonXmlProperty(isAttribute = true)
    val palmMute : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val pluck : Byte = -1,
    @JacksonXmlProperty(isAttribute = true)
    val pullOff : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val slap : Byte = -1,
    @JacksonXmlProperty(isAttribute = true)
    val slideTo : Byte = -1,
    @JacksonXmlProperty(isAttribute = true)
    val string : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val sustain : Float,
    @JacksonXmlProperty(isAttribute = true)
    val tremolo : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val harmonicPinch : Int,
    @JacksonXmlProperty(isAttribute = true)
    val pickDirection : Int,
    @JacksonXmlProperty(isAttribute = true)
    val rightHand : Byte = -1,
    @JacksonXmlProperty(isAttribute = true)
    val slideUnpitchTo : Byte = -1,
    @JacksonXmlProperty(isAttribute = true)
    val tap : Byte,
    @JacksonXmlProperty(isAttribute = true)
    val vibrato : Short,
    @JacksonXmlProperty(localName = "bendValue")
    @JacksonXmlElementWrapper(localName = "bendValues")
    val bendValues : List<BendValue>
)
