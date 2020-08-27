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

package eu.tilk.cdlcplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.tilk.cdlcplayer.song.Song2014

@Entity(tableName = "Arrangement")
data class Arrangement(
    @PrimaryKey
    val persistentID : String,
    val key : String,
    val arrangement : String,
    val part : Int,
    val capo : Int,
    val tuning : String
) {
    constructor(song : Song2014) : this(
        song.persistentID,
        song.songKey,
        song.arrangement,
        song.part,
        song.capo,
        song.tuning.name()
    )
}