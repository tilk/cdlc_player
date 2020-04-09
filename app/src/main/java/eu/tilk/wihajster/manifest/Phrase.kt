package eu.tilk.wihajster.manifest

import com.fasterxml.jackson.annotation.JsonProperty

class Phrase {
    @JsonProperty("MaxDifficulty")
    var maxDifficulty : Int = 0
    @JsonProperty("Name")
    var name : String = ""
    @JsonProperty("IterationCount")
    var iterationCount : Int = 0
}
