package eu.tilk.wihajster.manifest

import com.fasterxml.jackson.annotation.JsonProperty

class PhraseIteration {
    @JsonProperty("PhraseIndex")
    var phraseIndex : Int = 0
    @JsonProperty("MaxDifficulty")
    var maxDifficulty : Int = 0
    @JsonProperty("Name")
    var name : String = ""
    @JsonProperty("StartTime")
    var startTime : Float = 0f
    @JsonProperty("EndTime")
    var endTime : Float = 0f
    @JsonProperty("MaxScorePerDifficulty")
    var maxScorePerDifficulty : List<Float> = ArrayList()
}
