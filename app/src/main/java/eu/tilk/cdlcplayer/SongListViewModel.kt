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

package eu.tilk.cdlcplayer

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import eu.tilk.cdlcplayer.data.*
import eu.tilk.cdlcplayer.psarc.PSARCReader
import eu.tilk.cdlcplayer.song.Song2014
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

class SongListViewModel(private val app : Application) : AndroidViewModel(app) {
    private val database = SongRoomDatabase.getDatabase(app)
    private val songDao : SongDao = SongRoomDatabase.getDatabase(app).songDao()
    private val arrangementDao : ArrangementDao = SongRoomDatabase.getDatabase(app).arrangementDao()
    private fun exceptionHandler(handler : (Throwable) -> Unit) =
        CoroutineExceptionHandler{_ , throwable ->
            viewModelScope.launch(Dispatchers.Main) {
                handler(throwable)
            }
        }
    @ExperimentalUnsignedTypes
    fun decodeAndInsert(uri : Uri, handler : (Throwable?) -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val outputFile = File(app.cacheDir, "output.CoroutineExceptionHandler {psarc")
                app.contentResolver.openInputStream(uri).use { input ->
                    if (input != null) FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
                FileInputStream(outputFile).use { stream ->
                    outputFile.delete()
                    val psarc = PSARCReader(stream)
                    val songs = ArrayList<Song2014>()
                    for (f in psarc.listFiles("""manifests/.*\.json""".toRegex())) {
                        val baseNameMatch =
                            """manifests/.*/([^/]*)\.json""".toRegex().matchEntire(f)
                        val baseName = baseNameMatch!!.groupValues[1]
                        val manifest = psarc.inflateManifest(f)
                        val attributes = manifest.entries.values.first().values.first()
                        when (attributes.arrangementName) {
                            "Lead", "Rhythm", "Bass" ->
                                songs.add(
                                    psarc.inflateSng(
                                        "songs/bin/generic/$baseName.sng",
                                        attributes
                                    )
                                )
                        }
                    }
                    insert(songs)
                }
                withContext(Dispatchers.Main) { handler(null) }
            } catch (throwable : Throwable) {
                withContext(Dispatchers.Main) { handler(throwable) }
            }
        }

    private suspend fun insert(songs : List<Song2014>) {
        for (song in songs) {
            app.openFileOutput("${song.persistentID}.xml", Context.MODE_PRIVATE).use {
                it.write(
                    XmlMapper().registerModule(KotlinModule())
                        .writeValueAsBytes(song)
                )
            }
        }
        database.withTransaction {
            for (song in songs) arrangementDao.insert(Arrangement(song))
            songDao.insert(Song(songs[0]))
        }
    }

    enum class SortOrder {
        TITLE, ARTIST, ALBUM_NAME, ALBUM_YEAR
    }

    private var currentList = songDao.getSongsByTitle()
        set(value) {
            listMediator.removeSource(currentList)
            field = value
            listMediator.addSource(value) { listMediator.value = it }
        }
    private val listMediator = MediatorLiveData<List<SongWithArrangements>>().apply {
        addSource(currentList) { value = it }
    }

    val list : LiveData<List<SongWithArrangements>> get() = listMediator

    var sortOrder : SortOrder = SortOrder.TITLE
        set(value) {
            field = value
            updateList()
        }

    var search : String = ""
        set(value) {
            field = value
            updateList()
        }

    private fun updateList() {
        currentList = if (search == "")
            when (sortOrder) {
                SortOrder.TITLE -> songDao.getSongsByTitle()
                SortOrder.ARTIST -> songDao.getSongsByArtist()
                SortOrder.ALBUM_NAME -> songDao.getSongsByAlbumName()
                SortOrder.ALBUM_YEAR -> songDao.getSongsByAlbumYear()
            }
        else
            when (sortOrder) {
                SortOrder.TITLE -> songDao.getSongsByTitleSearch("%$search%")
                SortOrder.ARTIST -> songDao.getSongsByArtistSearch("%$search%")
                SortOrder.ALBUM_NAME -> songDao.getSongsByAlbumNameSearch("%$search%")
                SortOrder.ALBUM_YEAR -> songDao.getSongsByAlbumYearSearch("%$search%")
            }
    }
}