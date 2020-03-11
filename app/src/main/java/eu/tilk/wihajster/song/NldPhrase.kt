package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class NldPhrase(
    @JacksonXmlProperty(isAttribute = true)
    val id : Int
)
