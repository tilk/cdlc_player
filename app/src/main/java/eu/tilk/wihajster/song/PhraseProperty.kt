package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class PhraseProperty(
    @JacksonXmlProperty(isAttribute = true, localName = "phraseId")
    var phraseId : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "redundant")
    var redundant : Short,
    @JacksonXmlProperty(isAttribute = true, localName = "levelJump")
    var levelJump : Short,
    @JacksonXmlProperty(isAttribute = true, localName = "empty")
    var empty : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "difficulty")
    var difficulty : Int
)
