package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class LinkedDiff(
    @JacksonXmlProperty(isAttribute = true)
    val parentId : Int,
    @JacksonXmlProperty(isAttribute = true)
    val childId : Int
)