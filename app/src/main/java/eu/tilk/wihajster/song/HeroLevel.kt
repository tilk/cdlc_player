package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class HeroLevel(
    @JacksonXmlProperty(isAttribute = true)
    val difficulty : Int,
    @JacksonXmlProperty(isAttribute = true)
    val hero : Int
)
