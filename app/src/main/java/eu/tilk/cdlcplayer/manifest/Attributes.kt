/*
 *     Copyright (C) 2020  Marek Materzok
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package eu.tilk.cdlcplayer.manifest

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
    @JsonProperty("Tone_A")
    var toneA : String = ""
    @JsonProperty("Tone_B")
    var toneB : String = ""
    @JsonProperty("Tone_C")
    var toneC : String = ""
    @JsonProperty("Tone_D")
    var toneD : String = ""
    @JsonProperty("Tone_Base")
    var toneBase : String = ""
}