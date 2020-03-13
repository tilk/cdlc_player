package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class Level2014 {
    @JacksonXmlProperty(isAttribute = true, localName = "difficulty")
    var difficulty: Int = -1
    @JacksonXmlProperty(localName = "note")
    @JacksonXmlElementWrapper(localName = "notes")
    var notes: List<Note2014> = ArrayList()
    @JacksonXmlProperty(localName = "chord")
    @JacksonXmlElementWrapper(localName = "chords")
    var chords: List<Chord2014> = ArrayList()
    @JacksonXmlProperty(localName = "anchor")
    @JacksonXmlElementWrapper(localName = "anchors")
    var anchors: List<Anchor2014> = ArrayList()
    @JacksonXmlProperty(localName = "handShape")
    @JacksonXmlElementWrapper(localName = "handShapes")
    var handShapes: List<HandShape> = ArrayList()
}