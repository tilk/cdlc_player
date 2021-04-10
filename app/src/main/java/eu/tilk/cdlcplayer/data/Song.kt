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

import androidx.room.*
import eu.tilk.cdlcplayer.song.Song2014

@Entity(tableName = "Song")
data class Song(
    @PrimaryKey
    val key : String,
    val title : String,
    @ColumnInfo(index = true)
    val songNameSort : String,
    val artistName : String,
    val artistNameSort : String,
    val songLength : Float,
    val albumName : String,
    @ColumnInfo(index = true)
    val albumNameSort : String,
    @ColumnInfo(index = true)
    val albumYear : Int,
    val tuning : String
) {
    constructor(song : Song2014) : this(
        song.songKey,
        song.title,
        song.songNameSort,
        song.artistName,
        song.artistNameSort,
        song.songLength,
        song.albumName,
        song.albumNameSort,
        song.albumYear,
        song.tuning.name()
    )
}