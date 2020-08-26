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

package eu.tilk.wihajster.viewer

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import eu.tilk.wihajster.R
import eu.tilk.wihajster.shapes.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.tan
import android.opengl.GLES31.*
import androidx.preference.PreferenceManager
import eu.tilk.wihajster.song.Song2014

class SongGLRenderer(val data : Song2014, private val context : Context) :
    GLSurfaceView.Renderer {
    val song : List<Event> = data.makeEventList()
    private lateinit var textures : Textures
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private var lastFrameTime : Long = 0
    private var scrollSpeed : Float = 13f
    private lateinit var scroller : SongScroller
    private var awayAnchor : Event.Anchor =
        Event.Anchor(0f, 1, 4)
    private var finalAnchor : Event.Anchor = awayAnchor
    private var eyeX : Float = 2f
    private var eyeY : Float = 1.2f
    private var eyeZ : Float = 3f
    private val sounds : SoundPool = SoundPool.Builder()
        .run {
        setAudioAttributes(AudioAttributes.Builder().run {
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            build()
        })
        setMaxStreams(2)
        build()
    }
    private val metronome1 = sounds.load(context,
        R.raw.metronome1, 1)
    private val metronome2 = sounds.load(context,
        R.raw.metronome2, 1)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        //glEnable(GL_DEPTH_TEST)
        textures = Textures(context, data)
        Neck.initialize()
        NeckInlays.initialize()
        Frets.initialize()
        FretNumbers.initialize()
        Anchor.initialize()
        Note.initialize()
        NoteTail.initialize()
        EmptyStringNote.initialize()
        Beat.initialize()
        Chord.initialize()
        ChordInfo.initialize(textures, data.chordTemplates.size)
        ChordSustain.initialize()
        lastFrameTime = SystemClock.elapsedRealtime()
        scroller = SongScroller(song, 40f, scrollSpeed)
    }

    override fun onDrawFrame(gl: GL10?) {
        val currentTime = SystemClock.elapsedRealtime()
        val deltaTime = currentTime - lastFrameTime
        lastFrameTime = currentTime

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val clickPref = sharedPreferences.getString("click", "on_beats")

        fun play(sound : Int) {
            sounds.play(sound, 1f, 1f, 0, 0, 1f)
        }

        scroller.advance(deltaTime / 1000.0F) { evt : Event, derived : Boolean ->
            when (evt) {
                is Event.Beat ->
                    if (clickPref == "on_beats")
                      play(if (evt.measure >= 0) metronome2 else metronome1)
                is Event.Note ->
                    if (clickPref == "on_notes" && !derived)
                        play(metronome1)
                is Event.Chord ->
                    if (clickPref == "on_notes" && !derived)
                        play(metronome1)
                is Event.Anchor ->
                    finalAnchor = evt
            }
        }

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        Matrix.setLookAtM(
            viewMatrix,
            0,
            eyeX,
            eyeY * 2.1f,
            eyeZ,
            eyeX,
            eyeY * 1.1f,
            0f,
            0f,
            1.0f,
            0.0f
        )
        Matrix.multiplyMM(
            vPMatrix,
            0,
            projectionMatrix,
            0,
            viewMatrix,
            0
        )

        fun draw(shape : Shape) {
            shape.draw(vPMatrix, scroller.currentTime, scrollSpeed)
        }

        var leftFret = 24
        var rightFret = 1
        var frontLeftFret = 24
        var frontRightFret = 1
        var activeStrings = 0
        for (shape in scroller.activeEvents.sortedWith(compareBy<EventShape<Event>>{ it.sortLevel.level }.thenByDescending(
            EventShape<Event>::endTime))) {
            draw(shape)
            when (val evt = shape.event) {
                is Event.Anchor -> {
                    frontLeftFret = evt.fret.toInt()
                    frontRightFret = evt.fret + evt.width
                    if (evt.fret < leftFret) leftFret = frontLeftFret
                    if (evt.fret + evt.width > rightFret) rightFret = frontRightFret
                }
                is Event.Note -> {
                    activeStrings = activeStrings or (1 shl evt.string.toInt())
                }
            }
        }

        val targetEyeX = (leftFret + rightFret)/2.0f - 1f
        val targetEyeY = (rightFret - leftFret + 2)/6.0f*1.2f
        val targetEyeZ = (rightFret - leftFret + 2)/6.0f*3f
        eyeX = 0.02f*targetEyeX + 0.98f*eyeX
        eyeY = 0.02f*targetEyeY + 0.98f*eyeY
        eyeZ = 0.02f*targetEyeZ + 0.98f*eyeZ

        draw(Neck(activeStrings))
        draw(NeckInlays(frontLeftFret, frontRightFret))
        draw(Frets())
        draw(FretNumbers(
            textures,
            frontLeftFret,
            frontRightFret
        ))

        if (scroller.currentTime > data.songLength) (context as Activity)!!.finish()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        val fov = 0.9f
        val zNear = 0.1f
        val zFar = 100.0f
        val size = zNear * tan(fov / 2)
        Matrix.frustumM(
            projectionMatrix, 0,
            -size * ratio, size * ratio, -size, size, zNear, zFar
        )
    }
}