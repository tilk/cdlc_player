package eu.tilk.wihajster.manifest

import com.fasterxml.jackson.annotation.JsonProperty

class Manifest {
    @JsonProperty("Entries")
    var entries : Map<String, Map<String, Attributes>> = HashMap()
    @JsonProperty("ModelName")
    var modelName : String = ""
    @JsonProperty("IterationVersion")
    var iterationVersion : Int = -1
}