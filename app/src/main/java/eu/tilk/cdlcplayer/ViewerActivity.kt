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

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import eu.tilk.cdlcplayer.viewer.SongGLSurfaceView

class ViewerActivity : AppCompatActivity() {
    private val songViewModel : SongViewModel by viewModels()

    private lateinit var glView : GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val songId = intent.getStringExtra(SONG_ID)
        songViewModel.song.observe(this) { song ->
            glView = SongGLSurfaceView(this, song)
            setContentView(glView)
        }
        songViewModel.loadSong(songId!!)
    }

    companion object {
        const val SONG_ID = "eu.tilk.wihajster.SONG_ID"
    }
}
