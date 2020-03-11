package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class PhraseIteration2014(
    @JacksonXmlProperty(isAttribute = true)
    val variation : String,
    @JacksonXmlProperty(localName = "heroLevel")
    @JacksonXmlElementWrapper(localName = "heroLevels")
    val heroLevels : List<HeroLevel>
)
