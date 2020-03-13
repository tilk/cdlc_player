package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class HeroLevel(
    @JacksonXmlProperty(isAttribute = true, localName = "difficulty")
    var difficulty : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "hero")
    var hero : Int
)
