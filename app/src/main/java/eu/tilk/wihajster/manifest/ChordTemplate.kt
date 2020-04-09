package eu.tilk.wihajster.manifest

import com.fasterxml.jackson.annotation.JsonProperty

class ChordTemplate {
    @JsonProperty("ChordId")
    var chordId : Int = 0
    @JsonProperty("ChordName")
    var chordName : String = ""
    @JsonProperty("Fingers")
    var fingers : List<Int> = ArrayList()
    @JsonProperty("Frets")
    var frets : List<Int> = ArrayList()
}
