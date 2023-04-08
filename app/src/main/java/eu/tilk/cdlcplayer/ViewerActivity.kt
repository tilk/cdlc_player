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
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.material.button.MaterialButton
import eu.tilk.cdlcplayer.song.Song2014
import eu.tilk.cdlcplayer.viewer.RepeaterInfo
import eu.tilk.cdlcplayer.viewer.SongGLSurfaceView
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class ViewerActivity : AppCompatActivity() {
    private val songViewModel : SongViewModel by viewModels()

    private lateinit var glView : SongGLSurfaceView

    private val observer : Observer<Song2014> by lazy {
        Observer {
            setContentView(constructView())
            songViewModel.song.removeObserver(observer)
        }
    }

    private var player : ExoPlayer? = null

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                val wav = File(this.filesDir, "${songViewModel.song.value!!.songKey}.wem.wav")
                val mediaItem = MediaItem.fromUri(wav.toUri())
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            }
    }

    private fun constructView() : FrameLayout {
        initializePlayer()
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
        val pauseButton = pausedUI.findViewById<MaterialButton>(R.id.pauseButton)
        val speedBar = pausedUI.findViewById<SeekBar>(R.id.speedBar)
        val repStartButton = pausedUI.findViewById<MaterialButton>(R.id.repStartButton)
        val repEndButton = pausedUI.findViewById<MaterialButton>(R.id.repEndButton)
        val speedText = pausedUI.findViewById<TextView>(R.id.speedText)
        fun setVisibility(v : Int) {
            speedBar.visibility = v
            repStartButton.visibility = v
            repEndButton.visibility = v
            speedText.visibility = v
        }
        speedBar.max = 99
        pauseButton.setOnClickListener {
            songViewModel.paused.value = !songViewModel.paused.value!!
        }
        repStartButton.setOnClickListener {
            val beats = glView.nextBeats()
            val rep = songViewModel.repeater.value
            val beats2 = beats.take(2).toList()
            if (rep != null) {
                if (beats2.count() == 2 && beats2[0].time < rep.endBeat.time) {
                    songViewModel.repeater.value = rep.copy(
                            startBeat = beats2[0],
                            beatPeriod = beats2[1].time - beats2[0].time
                    )
                } else {
                    songViewModel.repeater.value = null
                }
            } else {
                if (beats2.count() == 2)
                    songViewModel.repeater.value =
                        RepeaterInfo(beats2[0], beats2[1], beats2[1].time - beats2[0].time)
            }
        }
        repEndButton.setOnClickListener {
            val beats = glView.nextBeats()
            val rep = songViewModel.repeater.value
            if (rep != null) {
                val beat1 = beats.firstOrNull()
                if (beat1 != null && beat1.time > rep.startBeat.time) {
                    songViewModel.repeater.value = rep.copy(endBeat = beat1)
                } else {
                    songViewModel.repeater.value = null
                }
            }
        }
        speedBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar : SeekBar, progress : Int, fromUser : Boolean) {
                if (fromUser)
                    songViewModel.speed.value = (progress + 1) / 100f
            }

            override fun onStartTrackingTouch(p0 : SeekBar?) { }
            override fun onStopTrackingTouch(p0 : SeekBar?) { }
        })
        songViewModel.paused.observeAndCall(this) {
            if (it) player!!.pause() else player!!.play()
            val resource =
                if (it) android.R.drawable.ic_media_play
                else android.R.drawable.ic_media_pause
            pauseButton.setIconResource(resource)
            setVisibility(if (it) View.VISIBLE else View.INVISIBLE)
        }
        songViewModel.speed.observeAndCall(this) {
            speedBar.progress = max(0, (100f * it).roundToInt() - 1)
            @SuppressLint("SetTextI18n")
            speedText.text = "${(100f*it).roundToInt()}%"
            player!!.setPlaybackSpeed(it)
        }
        songViewModel.repeater.observeAndCall(this) {
            repEndButton.isEnabled = it != null
        }
        songViewModel.seekpos.observeAndCall(this) {
            if (abs(player!!.currentPosition - it) > 100) player!!.seekTo(it)
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
            setContentView(R.layout.activity_viewer_loading);
            songViewModel.song.observe(this, observer)
            songViewModel.loadSong(songId!!)
        }
    }

    // Not so sure about this
    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        player?.release()
    }

    companion object {
        const val SONG_ID = "eu.tilk.cdlcplayer.SONG_ID"
    }
}
