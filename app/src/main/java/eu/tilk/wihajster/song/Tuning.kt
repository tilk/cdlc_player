package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class Tuning(
    @JacksonXmlProperty(isAttribute = true, localName = "string0")
    var string0 : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "string1")
    var string1 : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "string2")
    var string2 : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "string3")
    var string3 : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "string4")
    var string4 : Int,
    @JacksonXmlProperty(isAttribute = true, localName = "string5")
    var string5 : Int
) {
    constructor(strings : List<Short>) : this(
        (strings.getOrNull(0) ?: -1).toInt(),
        (strings.getOrNull(1) ?: -1).toInt(),
        (strings.getOrNull(2) ?: -1).toInt(),
        (strings.getOrNull(3) ?: -1).toInt(),
        (strings.getOrNull(4) ?: -1).toInt(),
        (strings.getOrNull(5) ?: -1).toInt()
    ){}
}
