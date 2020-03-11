package eu.tilk.wihajster.song

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("tuning")
data class Tuning(
    @JsonProperty("string0")
    val string0 : Int,
    @JsonProperty("string1")
    val string1 : Int,
    @JsonProperty("string2")
    val string2 : Int,
    @JsonProperty("string3")
    val string3 : Int,
    @JsonProperty("string4")
    val string4 : Int,
    @JsonProperty("string5")
    val string5 : Int
)
