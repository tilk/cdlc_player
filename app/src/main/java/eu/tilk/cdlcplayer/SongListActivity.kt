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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import eu.tilk.cdlcplayer.data.SongWithArrangements
import eu.tilk.cdlcplayer.psarc.PSARCReader
import eu.tilk.cdlcplayer.song.Song2014
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SongListActivity: AppCompatActivity() {
    private lateinit var songViewModel : SongViewModel
    private lateinit var observer : Observer<List<SongWithArrangements>>
    private lateinit var data : LiveData<List<SongWithArrangements>>

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list);
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val emptyView = findViewById<TextView>(R.id.empty_view)
        emptyView.movementMethod = LinkMovementMethod.getInstance()
        val adapter = SongListAdapter(this) { _, arrangement ->
            val intent = Intent(this, ViewerActivity::class.java).apply {
                putExtra(ViewerActivity.SONG_ID, arrangement.persistentID)
            }
            startActivity(intent)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            emptyView.text = Html.fromHtml(getString(R.string.no_songs), Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            emptyView.text = Html.fromHtml(getString(R.string.no_songs))
        }
        fun emptyViewVisible(b : Boolean) {
            if (b) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
        recyclerView.adapter = adapter
        observer = Observer { songs ->
            songs?.let { adapter.setSongs(it); emptyViewVisible(it.isEmpty()) }
        }
        songViewModel = ViewModelProvider(this).get(SongViewModel::class.java)
        data = songViewModel.listByTitle()
        data.observe(this, observer)
    }

    override fun onCreateOptionsMenu(menu : Menu?) : Boolean {
        menuInflater.inflate(R.menu.song_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        val id = item.itemId
        if (id == R.id.add_song) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, READ_REQUEST_CODE)
            return true
        }
        if (id == R.id.settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        if (id == R.id.about) {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
        fun sort(newData : LiveData<List<SongWithArrangements>>) {
            data.removeObserver(observer)
            data = newData
            data.observe(this, observer)
        }
        if (id == R.id.sortByAlbumName) {
            sort(songViewModel.listByAlbumName())
        }
        if (id == R.id.sortByAlbumYear) {
            sort(songViewModel.listByAlbumYear())
        }
        if (id == R.id.sortByArtist) {
            sort(songViewModel.listByArtist())
        }
        if (id == R.id.sortByTitle) {
            sort(songViewModel.listByTitle())
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val url = data.data
            if (url != null) {
                val outputFile = File(this.cacheDir, "output.psarc")
                contentResolver.openInputStream(url).use { input ->
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
                    songViewModel.insert(songs).start()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val READ_REQUEST_CODE = 42
    }
}