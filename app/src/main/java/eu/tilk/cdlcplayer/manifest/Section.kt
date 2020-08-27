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
