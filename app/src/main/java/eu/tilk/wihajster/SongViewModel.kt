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
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import eu.tilk.wihajster.data.Song
import eu.tilk.wihajster.data.SongDao
import eu.tilk.wihajster.data.SongRoomDatabase
import eu.tilk.wihajster.song.Song2014
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongViewModel(private val app : Application) : AndroidViewModel(app) {
    private val dao : SongDao = SongRoomDatabase.getDatabase(app).songDao()

    fun insert(song : Song2014) = viewModelScope.launch(Dispatchers.IO) {
        dao.insert(Song(song))
        app.openFileOutput("${song.persistentID}.xml", Context.MODE_PRIVATE).use {
            it.write(XmlMapper().registerModule(KotlinModule())
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .writeValueAsBytes(song))
        }
    }

    fun list() = dao.getSongsByTitle()
}