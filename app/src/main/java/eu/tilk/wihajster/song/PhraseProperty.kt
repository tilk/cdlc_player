package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class PhraseProperty(
    @JacksonXmlProperty(isAttribute = true)
    val phraseId : Int,
    @JacksonXmlProperty(isAttribute = true)
    val redundant : Short,
    @JacksonXmlProperty(isAttribute = true)
    val levelJump : Short,
    @JacksonXmlProperty(isAttribute = true)
    val empty : Int,
    @JacksonXmlProperty(isAttribute = true)
    val difficulty : Int
)
