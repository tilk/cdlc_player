package eu.tilk.wihajster.manifest

import com.fasterxml.jackson.annotation.JsonProperty

class Attributes {
    @JsonProperty("AlbumArt")
    var albumArt : String = ""
    @JsonProperty("AlbumName")
    var albumName : String = ""
    @JsonProperty("AlbumNameSort")
    var albumNameSort : String = ""
    @JsonProperty("ArrangementName")
    var arrangementName : String = ""
    @JsonProperty("ArrangementType")
    var arrangementType : Int = 0
    @JsonProperty("ArrangementProperties")
    var arrangementProperties : Map<String, Int> = HashMap()
    @JsonProperty("ArtistName")
    var artistName : String = ""
    @JsonProperty("ArtistNameSort")
    var artistNameSort : String = ""
    @JsonProperty("BlockAsset")
    var blockAsset : String = ""
    @JsonProperty("CentOffset")
    var centOffset : Float = 0f
    @JsonProperty("ChordTemplates")
    var chordTemplates : List<ChordTemplate> = ArrayList()
    @JsonProperty("DynamicVisualDensity")
    var dynamicVisualDensity : List<Float> = ArrayList()
    @JsonProperty("FullName")
    var fullName : String = ""
    @JsonProperty("LastConversionDateTime")
    var lastConversionDateTime : String = ""
    @JsonProperty("MaxPhraseDifficulty")
    var maxPhraseDifficulty : Int = 0
    @JsonProperty("PersistentID")
    var persistentID : String = ""
    @JsonProperty("PhraseIterations")
    var phraseIterations : List<PhraseIteration> = ArrayList()
    @JsonProperty("Phrases")
    var phrases : List<Phrase> = ArrayList()
    @JsonProperty("Sections")
    var sections : List<Section> = ArrayList()
    @JsonProperty("SongAsset")
    var songAsset : String = ""
    @JsonProperty("SongAverageTempo")
    var songAverageTempo : Float = 0f
    @JsonProperty("SongDifficulty")
    var songDifficulty : Float = 0f
    @JsonProperty("SongEvent")
    var songEvent : String = ""
    @JsonProperty("SongKey")
    var songKey : String = ""
    @JsonProperty("SongLength")
    var songLength : Float = 0f
    @JsonProperty("SongName")
    var songName : String = ""
    @JsonProperty("SongNameSort")
    var songNameSort : String = ""
    @JsonProperty("SongOffset")
    var songOffset : Float = 0f
    @JsonProperty("SongPartition")
    var songPartition : Int = 0
    @JsonProperty("SongYear")
    var songYear : Int = 0
    @JsonProperty("VocalsAssetId")
    var vocalsAssetId : String = ""
    @JsonProperty("FirstArrangementInSong")
    var firstArrangementInSong : Boolean = false
}