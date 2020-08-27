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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class NewLinkedDiff() {
    constructor(levelBreak : Int, ratio : String, phraseCount : Int, nldPhrases : List<NldPhrase>) : this() {
        this.levelBreak = levelBreak
        this.ratio = ratio
        this.phraseCount = phraseCount
        this.nldPhrases = nldPhrases
    }
    @JacksonXmlProperty(isAttribute = true, localName = "levelBreak")
    var levelBreak : Int = -1

    @JacksonXmlProperty(isAttribute = true, localName = "ratio")
    var ratio : String = ""

    @JacksonXmlProperty(isAttribute = true, localName = "phraseCount")
    var phraseCount : Int = 0

    @JacksonXmlProperty(localName = "nld_phrase")
    @JacksonXmlElementWrapper(useWrapping = false)
    var nldPhrases : List<NldPhrase> = ArrayList()
}
