package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class EBeat(
    @JacksonXmlProperty(isAttribute = true)
    val time : Float,
    @JacksonXmlProperty(isAttribute = true)
    val measure : Short = -1
)
