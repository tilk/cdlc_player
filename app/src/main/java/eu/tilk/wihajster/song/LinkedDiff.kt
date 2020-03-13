package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class LinkedDiff(
    @JacksonXmlProperty(isAttribute = true, localName = "parentId")
    var parentId : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "childId")
    var childId : Int
)