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
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import eu.tilk.cdlcplayer.data.*
import eu.tilk.cdlcplayer.psarc.PSARCReader
import eu.tilk.cdlcplayer.song.Song2014
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SongListViewModel(private val app : Application) : AndroidViewModel(app) {
    private val database = SongRoomDatabase.getDatabase(app)
    private val songDao : SongDao = SongRoomDatabase.getDatabase(app).songDao()
    private val arrangementDao : ArrangementDao = SongRoomDatabase.getDatabase(app).arrangementDao()

    @ExperimentalUnsignedTypes
    fun decodeAndInsert(uri : Uri) = viewModelScope.launch(Dispatchers.IO) {
        val outputFile = File(app.cacheDir, "output.psarc")
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
                val baseNameMatch = """manifests/.*/([^/]*)\.json""".toRegex().matchEntire(f)
                val baseName = baseNameMatch!!.groupValues[1]
                Log.w("file", baseName)
                val manifest = psarc.inflateManifest(f)
                val attributes = manifest.entries.values.first().values.first()
                Log.w("arrangement", attributes.arrangementName)
                when (attributes.arrangementName) {
                    "Lead", "Rhythm" ->
                        songs.add(psarc.inflateSng(
                            "songs/bin/generic/$baseName.sng",
                            attributes))
                }
            }
            insert(songs).start()
        }
    }

    private fun insert(songs : List<Song2014>) = viewModelScope.launch(Dispatchers.IO) {
        for (song in songs) {
            arrangementDao.insert(Arrangement(song))
            app.openFileOutput("${song.persistentID}.xml", Context.MODE_PRIVATE).use {
                it.write(
                    XmlMapper().registerModule(KotlinModule())
                        .writeValueAsBytes(song)
                )
            }
        }
        database.withTransaction {
            songDao.insert(Song(songs[0]))
        }
    }

    fun listByTitle() = songDao.getSongsByTitle()
    fun listByArtist() = songDao.getSongsByArtist()
    fun listByAlbumName() = songDao.getSongsByAlbumName()
    fun listByAlbumYear() = songDao.getSongsByAlbumYear()
}