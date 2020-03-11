package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class NewLinkedDiff(
    @JacksonXmlProperty(isAttribute = true)
    val levelBreak : Int,
    @JacksonXmlProperty(isAttribute = true)
    val ratio : String,
    @JacksonXmlProperty(isAttribute = true)
    val phraseCount : Int,
    @JacksonXmlProperty(localName = "nld_phrase")
    @JacksonXmlElementWrapper(useWrapping = false)
    val nldPhrases : List<NldPhrase>
)
