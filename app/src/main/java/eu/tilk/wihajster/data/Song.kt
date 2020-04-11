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

package eu.tilk.wihajster.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import eu.tilk.wihajster.song.Song2014

@Entity(tableName = "Song")
data class Song(
    @PrimaryKey
    val persistentID : String,
    val title : String,
    @ColumnInfo(index = true)
    val songNameSort : String,
    val arrangement : String,
    val part : Int,
    val songLength : Float,
    val albumName : String,
    @ColumnInfo(index = true)
    val albumNameSort : String,
    @ColumnInfo(index = true)
    val albumYear : Int,
    val capo : Int,
    val tuning : String
) {
    constructor(song : Song2014) : this(
        song.persistentID,
        song.title,
        song.songNameSort,
        song.arrangement,
        song.part,
        song.songLength,
        song.albumName,
        song.albumNameSort,
        song.albumYear,
        song.capo,
        song.tuning.name()
    )
}