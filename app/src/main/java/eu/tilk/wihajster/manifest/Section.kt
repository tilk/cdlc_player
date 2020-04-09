package eu.tilk.wihajster.manifest

import com.fasterxml.jackson.annotation.JsonProperty

class Section {
    @JsonProperty("Name")
    var name : String = ""
    @JsonProperty("UIName")
    var uiName : String = ""
    @JsonProperty("Number")
    var number : Int = 0
    @JsonProperty("StartTime")
    var startTime : Float = 0f
    @JsonProperty("EndTime")
    var endTime : Float = 0f
    @JsonProperty("StartPhraseIterationIndex")
    var startPhraseIterationIndex : Int = 0
    @JsonProperty("EndPhraseIterationIndex")
    var endPhraseIterationIndex : Int = 0
    @JsonProperty("IsSolo")
    var isSolo : Boolean = false
}
