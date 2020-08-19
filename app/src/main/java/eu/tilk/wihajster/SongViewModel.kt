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

package eu.tilk.wihajster

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import eu.tilk.wihajster.data.*
import eu.tilk.wihajster.song.Song2014
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongViewModel(private val app : Application) : AndroidViewModel(app) {
    private val database = SongRoomDatabase.getDatabase(app)
    private val songDao : SongDao = SongRoomDatabase.getDatabase(app).songDao()
    private val arrangementDao : ArrangementDao = SongRoomDatabase.getDatabase(app).arrangementDao()

    fun insert(songs : List<Song2014>) = viewModelScope.launch(Dispatchers.IO) {
        database.withTransaction {
            songDao.insert(Song(songs[0]))
            for (song in songs) {
                arrangementDao.insert(Arrangement(song))
                app.openFileOutput("${song.persistentID}.xml", Context.MODE_PRIVATE).use {
                    it.write(
                        XmlMapper().registerModule(KotlinModule())
                            .writeValueAsBytes(song)
                    )
                }
            }
        }
    }

    fun list() = songDao.getSongsByTitle()
}