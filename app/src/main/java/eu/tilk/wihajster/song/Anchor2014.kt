package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Anchor2014(
    @JacksonXmlProperty(isAttribute = true)
    val width : Short
)
