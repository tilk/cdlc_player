package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class NewLinkedDiff(
    @get:JacksonXmlProperty(isAttribute = true, localName = "levelBreak")
    var levelBreak : Int,
    @get:JacksonXmlProperty(isAttribute = true, localName = "ratio")
    var ratio : String,
    @get:JacksonXmlProperty(isAttribute = true, localName = "phraseCount")
    var phraseCount : Int/* TODO,
    @get:JacksonXmlProperty(localName = "nld_phrase")
    @get:JacksonXmlElementWrapper(useWrapping = false)
    var nldPhrases : List<NldPhrase> = ArrayList()*/
)
