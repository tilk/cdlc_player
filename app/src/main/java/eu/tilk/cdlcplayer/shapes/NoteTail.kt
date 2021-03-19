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

package eu.tilk.cdlcplayer.shapes

import eu.tilk.cdlcplayer.viewer.Event
import eu.tilk.cdlcplayer.viewer.SortLevel
import android.opengl.GLES31.*
import eu.tilk.cdlcplayer.viewer.NoteInfo
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.sin

class NoteTail(note : Event.Note, val anchor : Event.Anchor, scrollSpeed : Float) :
    NoteyShape<Event.Note>(
        makeVertexCoords(note, anchor, scrollSpeed),
        makeDrawOrder(note, scrollSpeed),
        mProgram,
        note
    ) {
    companion object : CompanionBase(
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform vec4 uPosition;
            uniform float uMaxZ;
            in vec4 vPosition;
            in float vParity;
            out float zPos;
            out vec2 vTexCoord;
            void main() {
                vec4 pos = vPosition + uPosition;
                gl_Position = uMVPMatrix * pos;
                zPos = pos.z;
                vTexCoord = vec2(vParity, -vPosition.z / uMaxZ);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            uniform int uString;
            uniform float unpitched;
            in float zPos;
            in vec2 vTexCoord;
            out vec4 FragColor;
            $stringColorsGLSL
            $logisticGLSL
            void main() {
                float dist = abs(vTexCoord.x);
                float scaling = min(1.0, 1.0+(atan(1.0-20.0*abs(dist-0.8)))/3.14);
                FragColor = vec4(scaling * stringColors[uString], 
                    step(zPos, 0.0) * clamp(40.0 + zPos, 0.0, 1.0) * 
                        max(1.0 - unpitched, 1.0 - logistic(4.0*(vTexCoord.y - 0.5))));
            }
        """.trimIndent()
    ) {
        private const val scaling = 10
        private fun sizeFor(note : Event.Note, scrollSpeed : Float) : Int =
            ceil(note.sustain * scrollSpeed * scaling).toInt()
        private fun makeVertexCoords(note : Event.Note, anchor : Event.Anchor, scrollSpeed : Float) : FloatArray {
            val suswidth = if (note.fret > 0) 1
                           else anchor.width
            val sz = sizeFor(note, scrollSpeed)
            return FloatArray(6 + 6 * sz) {
                val i = it / 3 // vertex number
                val z = i / 2  // Z axis distance
                val pct = z.toFloat() / sz
                when (it % 3) {
                    0 -> {
                        var v = (suswidth - 0.75f) * ((i % 2).toFloat() - 0.5f)
                        // add slide effect
                        if (note.slideLen != 0)
                            v = v * (1f + 10f * dLogistic(pct * 10f - 5f) / note.sustain / scrollSpeed * note.slideLen) +
                                note.slideValue(pct)
                        // add tremolo effect
                        if (note.tremolo)
                            when (z % 8) {
                                1,3 -> v += 0.05f
                                2 -> v += 0.1f
                                5,7 -> v -= 0.05f
                                6 -> v -= 0.1f
                            }
                        v
                    }
                    1 -> {
                        var v = 0f
                        // add vibrato effect
                        if (note.vibrato > 0) {
                            v += 0.15f * sin(4.0f * z.toFloat() / scaling) * sin(pct * PI.toFloat())
                        }
                        // add bend effect
                        if (note.bend.isNotEmpty()) {
                            v += 0.25f * note.bendValue(pct)
                        }
                        v
                    }
                    2 -> -z.toFloat() / scaling
                    else -> error("Should not happen!")
                }
            }
        }
        private val drawOrder = shortArrayOf(0, 1, 2, 1, 3, 2)
        private fun makeDrawOrder(note : Event.Note, scrollSpeed : Float) : ShortArray {
            return ShortArray(6 * sizeFor(note, scrollSpeed)) {
                (drawOrder[it % 6] + 2 * (it / 6)).toShort()
            }
        }
        private lateinit var calculator : NoteCalculator
        fun initialize(calculator: NoteCalculator) {
            super.initialize()
            this.calculator = calculator
        }
    }

    private val parityBuffer : FloatBuffer =
        ByteBuffer.allocateDirect(8 + sizeFor(note, scrollSpeed) * 8).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                for (i in 0..sizeFor(note, scrollSpeed)) {
                    put(-1f)
                    put(1f)
                }
                position(0)
            }
        }
    override val derived = true
    override val sortLevel =
        SortLevel.StringTail(calculator.sort(note.string.toInt()))

    override fun noteInfo(time: Float, scrollSpeed : Float) : NoteInfo? {
        val pct = (time - event.time) / event.sustain
        return if (event.fret > 0 && pct >= 0 && pct <= 1)
            NoteInfo(
                0f, event.fret, event.string,
                event.bendValue(pct),
                event.slideValue(pct)
            )
        else
            null
    }
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        val parityHandle = glGetAttribLocation(mProgram, "vParity").also {
            glEnableVertexAttribArray(it)
            glVertexAttribPointer(it, 1, GL_FLOAT, false, 4, parityBuffer)
        }
        glGetUniformLocation(mProgram, "uPosition").also {
            val x = if (event.fret > 0) calculator.calcX(event.fret)
                    else anchor.fret + anchor.width/2f - 1f
            glUniform4f(
                it,
                x,
                calculator.calcY(event.string),
                calculator.calcZ(event.time, time,  scrollSpeed),
                0f
            )
        }
        glGetUniformLocation(mProgram, "uString").also {
            glUniform1i(it, event.string.toInt())
        }
        glGetUniformLocation(mProgram, "uMaxZ").also {
            glUniform1f(it, event.sustain * scrollSpeed)
        }
        glGetUniformLocation(mProgram, "unpitched").also {
            glUniform1f(it, if (event.slideUnpitchedTo >= 0) 1f else 0f)
        }
        super.internalDraw(time, scrollSpeed)
        glDisableVertexAttribArray(parityHandle)
    }
}