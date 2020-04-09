package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class PhraseIteration2014() {
    constructor(variation : String, time : Float, phraseId : Int, heroLevels : List<HeroLevel>) : this() {
        this.variation = variation
        this.time = time
        this.phraseId = phraseId
        this.heroLevels = heroLevels
    }
    @JacksonXmlProperty(isAttribute = true, localName = "variation")
    var variation : String = ""
    @JacksonXmlProperty(isAttribute = true, localName = "time")
    var time : Float = 0f
    @JacksonXmlProperty(isAttribute = true, localName = "phraseId")
    var phraseId : Int = -1
    @JacksonXmlProperty(localName = "heroLevel")
    @JacksonXmlElementWrapper(localName = "heroLevels")
    var heroLevels : List<HeroLevel> = ArrayList()
}
