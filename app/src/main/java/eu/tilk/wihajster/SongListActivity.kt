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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.tilk.wihajster.psarc.PSARCReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SongListActivity: AppCompatActivity() {
    private lateinit var songViewModel : SongViewModel

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list);
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = SongListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        songViewModel = ViewModelProvider(this).get(SongViewModel::class.java)
        songViewModel.list().observe(this, Observer { songs ->
            songs?.let { adapter.setSongs(it) }
        })
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
                    for (f in psarc.listFiles("""manifests/.*\.json""".toRegex())) {
                        val baseNameMatch = """manifests/.*/([^/]*)\.json""".toRegex().matchEntire(f)
                        val baseName = baseNameMatch!!.groupValues[1]
                        Log.w("file", baseName)
                        val manifest = psarc.inflateManifest(f)
                        val attributes = manifest.entries.values.first().values.first()
                        Log.w("arrangement", attributes.arrangementName)
                        when (attributes.arrangementName) {
                            "Lead", "Rhythm" -> {
                                val sng = psarc.inflateSng("songs/bin/generic/$baseName.sng", attributes)
                                songViewModel.insert(sng).start()
                                Unit
                            }
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val READ_REQUEST_CODE = 42
    }
}