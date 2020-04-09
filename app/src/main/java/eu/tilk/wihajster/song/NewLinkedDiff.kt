package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class NewLinkedDiff() {
    constructor(levelBreak : Int, ratio : String, phraseCount : Int, nldPhrases : List<NldPhrase>) : this() {
        this.levelBreak = levelBreak
        this.ratio = ratio
        this.phraseCount = phraseCount
        this.nldPhrases = nldPhrases
    }
    @JacksonXmlProperty(isAttribute = true, localName = "levelBreak")
    var levelBreak : Int = -1

    @JacksonXmlProperty(isAttribute = true, localName = "ratio")
    var ratio : String = ""

    @JacksonXmlProperty(isAttribute = true, localName = "phraseCount")
    var phraseCount : Int = 0

    @JacksonXmlProperty(localName = "nld_phrase")
    @JacksonXmlElementWrapper(useWrapping = false)
    var nldPhrases : List<NldPhrase> = ArrayList()
}
