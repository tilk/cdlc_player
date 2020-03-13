package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Note2014(
    @JacksonXmlProperty(isAttribute = true, localName = "time")
    var time : Float,
    @JacksonXmlProperty(isAttribute = true, localName = "linkNext")
    var linkNext : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "accent")
    var accent : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "bend")
    var bend : Float,
    @JacksonXmlProperty(isAttribute = true, localName = "fret")
    var fret : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "hammerOn")
    var hammerOn : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "harmonic")
    var harmonic : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "hopo")
    var hopo : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "ignore")
    var ignore : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "leftHand")
    var leftHand : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "mute")
    var mute : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "palmMute")
    var palmMute : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "pluck")
    var pluck : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "pullOff")
    var pullOff : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "slap")
    var slap : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "slideTo")
    var slideTo : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "string")
    var string : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "sustain")
    var sustain : Float,
    @JacksonXmlProperty(isAttribute = true, localName = "tremolo")
    var tremolo : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "harmonicPinch")
    var harmonicPinch : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "pickDirection")
    var pickDirection : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "rightHand")
    var rightHand : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "slideUnpitchTo")
    var slideUnpitchTo : Byte = -1,
    @JacksonXmlProperty(isAttribute = true, localName = "tap")
    var tap : Byte,
    @JacksonXmlProperty(isAttribute = true, localName = "vibrato")
    var vibrato : Short/* TODO,
    @JacksonXmlProperty(localName = "bendValue")
    @JacksonXmlElementWrapper(localName = "bendValues")
    var bendValues : List<BendValue> = ArrayList()*/
)
