/*
 *     Copyright (C) 2020  Marek Materzok
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package eu.tilk.wihajster.song

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class Note2014() {
    constructor(
        time : Float,
        linkNext : Byte,
        linked : Byte,
        accent : Byte,
        bend : Float,
        fret : Byte,
        hammerOn : Byte,
        harmonic : Byte,
        hopo : Byte,
        ignore : Byte,
        leftHand : Byte,
        mute : Byte,
        palmMute : Byte,
        pluck : Byte,
        pullOff : Byte,
        slap : Byte,
        slideTo : Byte,
        string : Byte,
        sustain : Float,
        tremolo : Byte,
        harmonicPinch : Byte,
        pickDirection : Byte,
        rightHand : Byte,
        slideUnpitchTo : Byte,
        tap : Byte,
        vibrato : Short,
        bendValues : List<BendValue>
    ) : this() {
        this.time = time
        this.linkNext = linkNext
        this.linked = linked
        this.accent = accent
        this.bend = bend
        this.fret = fret
        this.hammerOn = hammerOn
        this.harmonic = harmonic
        this.hopo = hopo
        this.ignore = ignore
        this.leftHand = leftHand
        this.mute = mute
        this.palmMute = palmMute
        this.pluck = pluck
        this.pullOff = pullOff
        this.slap = slap
        this.slideTo = slideTo
        this.string = string
        this.sustain = sustain
        this.tremolo = tremolo
        this.harmonicPinch = harmonicPinch
        this.pickDirection = pickDirection
        this.rightHand = rightHand
        this.slideUnpitchTo = slideUnpitchTo
        this.tap = tap
        this.vibrato = vibrato
        this.bendValues = bendValues
    }
    @JacksonXmlProperty(isAttribute = true, localName = "time")
    var time : Float = 0f
    @JacksonXmlProperty(isAttribute = true, localName = "linkNext")
    var linkNext : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "linked")
    var linked : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "accent")
    var accent : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "bend")
    var bend : Float = 0f
    @JacksonXmlProperty(isAttribute = true, localName = "fret")
    var fret : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "hammerOn")
    var hammerOn : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "harmonic")
    var harmonic : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "hopo")
    var hopo : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "ignore")
    var ignore : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "leftHand")
    var leftHand : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "mute")
    var mute : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "palmMute")
    var palmMute : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "pluck")
    var pluck : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "pullOff")
    var pullOff : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "slap")
    var slap : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "slideTo")
    var slideTo : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "string")
    var string : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "sustain")
    var sustain : Float = 0f
    @JacksonXmlProperty(isAttribute = true, localName = "tremolo")
    var tremolo : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "harmonicPinch")
    var harmonicPinch : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "pickDirection")
    var pickDirection : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "rightHand")
    var rightHand : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "slideUnpitchTo")
    var slideUnpitchTo : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "tap")
    var tap : Byte = -1
    @JacksonXmlProperty(isAttribute = true, localName = "vibrato")
    var vibrato : Short = -1
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "bendValue")
    @JacksonXmlElementWrapper(localName = "bendValues")
    var bendValues : List<BendValue> = ArrayList()
}
