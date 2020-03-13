package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class PhraseIteration2014(
    @JacksonXmlProperty(isAttribute = true, localName = "variation")
    var variation : String/* TODO,
    @JacksonXmlProperty(localName = "heroLevel")
    @JacksonXmlElementWrapper(localName = "heroLevels")
    var heroLevels : List<HeroLevel> = ArrayList()*/
)
