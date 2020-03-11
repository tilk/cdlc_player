package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Level2014(
    @JacksonXmlProperty(isAttribute = true)
    val difficulty : Int,
    @JacksonXmlProperty(localName = "note")
    @JacksonXmlElementWrapper(localName = "notes")
    val notes : List<Note2014>,
    @JacksonXmlProperty(localName = "chord")
    @JacksonXmlElementWrapper(localName = "chords")
    val chords : List<Chord2014>,
    @JacksonXmlProperty(localName = "anchor")
    @JacksonXmlElementWrapper(localName = "anchors")
    val anchors : List<Anchor2014>,
    @JacksonXmlProperty(localName = "handShape")
    @JacksonXmlElementWrapper(localName = "handShapes")
    val handShapes : List<HandShape>
)