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

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.*
import androidx.media3.common.MediaItem
import androidx.media3.datasource.FileDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.metadata.MetadataOutput
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.text.TextOutput
import androidx.media3.exoplayer.video.VideoRendererEventListener
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.ogg.OggExtractor
import androidx.preference.PreferenceManager
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import com.google.android.material.button.MaterialButton
import eu.tilk.cdlcplayer.song.Song2014
import eu.tilk.cdlcplayer.song.Vocal
import eu.tilk.cdlcplayer.viewer.RepeaterInfo
import eu.tilk.cdlcplayer.viewer.SongGLSurfaceView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class ViewerActivity : AppCompatActivity() {
    private val songViewModel : SongViewModel by viewModels()

    private lateinit var glView : SongGLSurfaceView

    private val observer : Observer<Song2014> by lazy {
        Observer {
            setContentView(constructView())
            songViewModel.song.removeObserver(observer)
        }
    }

    private val secondObserver : Observer<Song2014> by lazy {
        Observer {
            playMusic()
            initAudioDispatcher()
            songViewModel.song.removeObserver(secondObserver)
        }
    }

    private var player : ExoPlayer? = null
    private var lyricsText : TextView? = null
    private var audioDispatcher : AudioDispatcher? = null
    private var audioThread : Thread? = null

    private fun initializePlayer() {
        val audioOnlyRenderersFactory =
            RenderersFactory {
                    handler: Handler,
                    videoListener: VideoRendererEventListener,
                    audioListener: AudioRendererEventListener,
                    textOutput: TextOutput,
                    metadataOutput: MetadataOutput,
                ->
                arrayOf(
                    MediaCodecAudioRenderer(this, MediaCodecSelector.DEFAULT, handler, audioListener)
                )
            }

        val customMediaSourceFactory = ProgressiveMediaSource.Factory(FileDataSource.Factory(),  ExtractorsFactory { arrayOf(OggExtractor()) })
            .setDrmSessionManagerProvider { DrmSessionManager.DRM_UNSUPPORTED }

        player = ExoPlayer.Builder(this, audioOnlyRenderersFactory, customMediaSourceFactory).build()
    }

    private fun playMusic() {
        val opus = File(this.filesDir, "${songViewModel.song.value!!.songKey}.opus")
        player!!.setMediaItem(MediaItem.fromUri(opus.toUri()))
        player!!.prepare()
        player!!.seekTo(if (this::glView.isInitialized) glView.currentTime() else 0)
        player!!.setPlaybackSpeed(songViewModel.speed.value!!)
        if (!songViewModel.paused.value!!) player!!.play()
    }

    private fun observeViewAndSyncMusic() {
        this.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    val currentTime = glView.currentTime()
                    val targetDelta = (250.0f*songViewModel.speed.value!! - 90.0f).roundToLong() // see https://www.wolframalpha.com/input?i=linear+regression+%7B%7B100%2C+180%7D%2C%7B90%2C145%7D%2C%7B80%2C+90%7D%2C%7B60%2C+50%7D%2C%7B40%2C+5%7D%2C%7B20%2C+-30%7D%2C%7B10%2C+-60%7D%7D
                    val actualDelta = currentTime - player!!.currentPosition
                    if (actualDelta !in (targetDelta - 70) .. (targetDelta + 70)) {
                        player!!.seekTo(currentTime)
                    }

                    if (songViewModel.song.value!!.vocals.isNotEmpty()) {
                        if (songViewModel.currentWord.value!! < 0 || currentTime / 1000F !in songViewModel.song.value!!.vocals[songViewModel.currentWord.value!!].time - 0.05 .. songViewModel.song.value!!.vocals[songViewModel.currentWord.value!!].time + songViewModel.song.value!!.vocals[songViewModel.currentWord.value!!].length + 0.05) {
                            songViewModel.currentWord.value =
                                songViewModel.song.value!!.vocals.binarySearch(Vocal(currentTime/1000F, 0, 0F, ""), Vocal::compareTo)
                            if (songViewModel.currentWord.value!! < 0) {
                                songViewModel.currentWord.value = -(songViewModel.currentWord.value!! + 2)
                            }
                        }

                        if (songViewModel.currentWord.value!! in 0 until songViewModel.song.value!!.vocals.size) {
                            if (songViewModel.currentWord.value!! == 0 || songViewModel.song.value!!.vocals[songViewModel.currentWord.value!! - 1].lyric.endsWith(
                                    "+"
                                )
                            ) {
                                songViewModel.sentenceStart.value =
                                    songViewModel.currentWord.value!!
                            }

                            var i = songViewModel.sentenceStart.value!!
                            var text = ""

                            while (i < songViewModel.song.value!!.vocals.size) {
                                val toAdd = songViewModel.song.value!!.vocals[i].lyric
                                if (i < songViewModel.currentWord.value!!) {
                                    text += "<font color='#999999'>"
                                }

                                if (i == songViewModel.currentWord.value) {
                                    text += "<b><font color='#fd686c'>"
                                }

                                text += toAdd.removeSuffix("+").removeSuffix("-")

                                if (i < songViewModel.currentWord.value!!) {
                                    text += "</font>"
                                }

                                if (i == songViewModel.currentWord.value) {
                                    text += "</font></b>"
                                }

                                if (!toAdd.endsWith("-")) {
                                    text += " "
                                }

                                i++
                                if (toAdd.endsWith("+")) {
                                    if (i - 1 == songViewModel.currentWord.value) {
                                        songViewModel.sentenceStart.value = i
                                    }
                                    break
                                }
                            }
                            lyricsText?.setText(Html.fromHtml(text))
                        }
                    }

                    delay(250)
                }
            }
        }
    }

    private fun constructView() : FrameLayout {
        glView = SongGLSurfaceView(this, songViewModel)
        observeViewAndSyncMusic()
        val frameLayout = FrameLayout(this)
        frameLayout.addView(glView)
        @SuppressLint("InflateParams")
        val pausedUI = layoutInflater.inflate(R.layout.song_paused_ui, null)
        val lyrics = layoutInflater.inflate(R.layout.lyrics, null)
        val missOrGood = layoutInflater.inflate(R.layout.miss_good, null)
        val ll = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        ll.gravity = Gravity.TOP or Gravity.FILL_HORIZONTAL
        pausedUI.layoutParams = ll
        frameLayout.addView(pausedUI)
        frameLayout.addView(lyrics)
        frameLayout.addView(missOrGood)
        val pauseButton = pausedUI.findViewById<MaterialButton>(R.id.pauseButton)
        val speedBar = pausedUI.findViewById<SeekBar>(R.id.speedBar)
        val repStartButton = pausedUI.findViewById<MaterialButton>(R.id.repStartButton)
        val repEndButton = pausedUI.findViewById<MaterialButton>(R.id.repEndButton)
        val speedText = pausedUI.findViewById<TextView>(R.id.speedText)
        val missOrGoodText = missOrGood.findViewById<TextView>(R.id.missOrGoodText)
        val scoreText = missOrGood.findViewById<TextView>(R.id.scoreText)
        lyricsText = lyrics.findViewById<TextView>(R.id.lyricsText)
        fun setVisibility(v : Int) {
            speedBar.visibility = v
            repStartButton.visibility = v
            repEndButton.visibility = v
            speedText.visibility = v
            val inverseV = if (v == View.VISIBLE) View.INVISIBLE else View.VISIBLE
            lyricsText!!.visibility = inverseV
            missOrGoodText!!.visibility = inverseV
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
            if (it) player?.pause() else player?.play()

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
            player?.setPlaybackSpeed(it)
        }
        songViewModel.repeater.observeAndCall(this) {
            repEndButton.isEnabled = it != null
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val detectNotes = sharedPreferences.getBoolean("detectNotes", false)
        if (detectNotes) {
            songViewModel.missGood.observeAndCall(this) {
                missOrGoodText.text = it.joinToString(" ")
            }

            songViewModel.totalNotes.observeAndCall(this) {
                val percentage = (100.0 * songViewModel.totalCorrectNotes.value!!) / it
                val score = songViewModel.totalCorrectNotes.value!! * 720
                scoreText.text = getString(R.string.score_accuracy, score.toString(), "%.2f".format(percentage) + "%")
            }
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

    override fun onRequestPermissionsResult(
        requestCode : Int,
        permissions : Array<String?>,
        grantResults : IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAudioDispatcher()
                } else {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                    sharedPreferences.edit().putBoolean("detectNotes", false).apply()
                }
            }
        }
    }

    private fun initAudioDispatcher() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val detectNotes = sharedPreferences.getBoolean("detectNotes", false)
        if (detectNotes && songViewModel.song.value!!.capo == 0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO
                )
            } else {
                audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
                val pdh = PitchDetectionHandler { res, _ ->
                    if (res.isPitched && res.probability > 0.91) {
                        runOnUiThread {
                            // println("GOT ${res.pitch} (${glView.currentTime()})")
                            songViewModel.currentNoteByPlayer.tryEmit(Pair(glView.currentTime(), res.pitch.toDouble()))
                        }
                    }
                }
                val pitchProcessor : AudioProcessor =
                    PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050f, 1024, pdh)
                audioDispatcher!!.addAudioProcessor(pitchProcessor)

                audioThread = Thread(audioDispatcher, "Audio Thread")
                audioThread!!.start()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        audioDispatcher?.stop()
        audioThread?.join()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
        if (songViewModel.song.value != null) {
            playMusic()
            initAudioDispatcher()
        } else {
            songViewModel.song.observe(this, secondObserver)
        }
    }

    companion object {
        const val SONG_ID = "eu.tilk.cdlcplayer.SONG_ID"
        const val REQUEST_RECORD_AUDIO = 33
    }
}
