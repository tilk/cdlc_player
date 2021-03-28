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

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import eu.tilk.cdlcplayer.song.Song2014
import eu.tilk.cdlcplayer.viewer.SongGLSurfaceView
import kotlin.math.max
import kotlin.math.roundToInt

class ViewerActivity : AppCompatActivity() {
    private val songViewModel : SongViewModel by viewModels()

    private lateinit var glView : GLSurfaceView

    private val observer : Observer<Song2014> by lazy {
        Observer {
            setContentView(constructView())
            songViewModel.song.removeObserver(observer)
        }
    }

    private fun constructView() : FrameLayout {
        glView = SongGLSurfaceView(this, songViewModel)
        val frameLayout = FrameLayout(this)
        frameLayout.addView(glView)
        @SuppressLint("InflateParams")
        val pausedUI = layoutInflater.inflate(R.layout.song_paused_ui, null)
        val ll = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        ll.gravity = Gravity.TOP or Gravity.FILL_HORIZONTAL
        pausedUI.layoutParams = ll
        frameLayout.addView(pausedUI)
        val pauseButton = pausedUI.findViewById<ImageButton>(R.id.pauseButton)
        val speedBar = pausedUI.findViewById<SeekBar>(R.id.speedBar)
        speedBar.max = 99
        pauseButton.setOnClickListener {
            songViewModel.paused.value = !songViewModel.paused.value!!
        }
        speedBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar : SeekBar, progress : Int, fromUser : Boolean) {
                if (fromUser)
                    songViewModel.speed.value = (progress + 1) / 100f
            }

            override fun onStartTrackingTouch(p0 : SeekBar?) { }
            override fun onStopTrackingTouch(p0 : SeekBar?) { }
        })
        songViewModel.paused.observe(this) {
            val resource =
                if (it) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play
            pauseButton.setImageResource(resource)
            speedBar.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
        songViewModel.speed.observe(this) {
            speedBar.progress = max(0, (100f * it).roundToInt() - 1)
        }
        return frameLayout
    }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val songId = intent.getStringExtra(SONG_ID)
        if (songViewModel.song.value != null)
            setContentView(constructView())
        else {
            songViewModel.song.observe(this, observer)
            songViewModel.loadSong(songId!!)
        }
    }

    companion object {
        const val SONG_ID = "eu.tilk.cdlcplayer.SONG_ID"
    }
}
