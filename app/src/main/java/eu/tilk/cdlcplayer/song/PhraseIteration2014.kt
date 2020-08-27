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

package eu.tilk.cdlcplayer.song

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class PhraseIteration2014() {
    constructor(variation : String, time : Float, phraseId : Int, heroLevels : List<HeroLevel>) : this() {
        this.variation = variation
        this.time = time
        this.phraseId = phraseId
        this.heroLevels = heroLevels
    }
    @JacksonXmlProperty(isAttribute = true, localName = "variation")
    var variation : String = ""
    @JacksonXmlProperty(isAttribute = true, localName = "time")
    var time : Float = 0f
    @JacksonXmlProperty(isAttribute = true, localName = "phraseId")
    var phraseId : Int = -1
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JacksonXmlProperty(localName = "heroLevel")
    @JacksonXmlElementWrapper(localName = "heroLevels")
    var heroLevels : List<HeroLevel> = ArrayList()
}
