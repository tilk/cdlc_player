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

package eu.tilk.wihajster.shapes

import eu.tilk.wihajster.viewer.Event
import eu.tilk.wihajster.viewer.SortLevel
import android.opengl.GLES31.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.math.tanh

class NoteTail(note : Event.Note, val anchor : Event.Anchor, scrollSpeed : Float) :
    EventShape<Event.Note>(
        makeVertexCoords(note, anchor, scrollSpeed),
        makeDrawOrder(note, scrollSpeed),
        mProgram,
        note
    ) {
    companion object {
        private const val scaling = 10
        private fun sizeFor(note : Event.Note, scrollSpeed : Float) : Int =
            ceil(note.sustain * scrollSpeed * scaling).toInt()
        private fun makeVertexCoords(note : Event.Note, anchor : Event.Anchor, scrollSpeed : Float) : FloatArray {
            val slideLen = when {
                note.slideTo > 0 -> note.slideTo - note.fret
                note.slideUnpitchedTo >= 0 -> note.slideUnpitchedTo - note.fret
                else -> 0
            }
            val suswidth = if (note.fret > 0) 1
                           else anchor.width
            val sz = sizeFor(note, scrollSpeed)
            fun logistic(x : Float) = 0.5f + 0.5f * tanh(x)
            fun dLogistic(x : Float) = logistic(x) * logistic(-x)
            return FloatArray(6 + 6 * sz) {
                val i = it / 3 // vertex number
                val z = i / 2  // Z axis distance
                val pct = z.toFloat() / sz
                when (it % 3) {
                    0 -> {
                        var v = (suswidth - 0.75f) * ((i % 2).toFloat() - 0.5f)
                        // add slide effect
                        if (slideLen != 0) v = v * (1f + 10f * dLogistic(pct * 10f - 5f) / note.sustain / scrollSpeed * slideLen) +
                            slideLen * logistic(pct * 10f - 5f)
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
                            val bi = note.bend.indexOfFirst { p -> p.first <= pct }
                            val start = if (bi == -1) Pair(0f, 0f) else note.bend[bi]
                            val end = if (note.bend.lastIndex < bi+1)
                                Pair(1f, note.bend.last().second) else note.bend[bi+1]
                            val prog = (pct - start.first)/(end.first - start.first)
                            val amnt = start.second + logistic(prog * 10f - 5f) *
                                    (end.second - start.second)
                            val dir = if (note.string > 2) -1f else 1f
                            v += 0.25f * amnt * dir
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
        private val vertexShaderCode = """
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
        """.trimIndent()
        private val fragmentShaderCode = """
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
        private var mProgram : Int = -1
        fun initialize() {
            val vertexShader =
                loadShader(
                    GL_VERTEX_SHADER,
                    vertexShaderCode
                )
            val fragmentShader = loadShader(
                GL_FRAGMENT_SHADER,
                fragmentShaderCode
            )
            mProgram = makeProgramFromShaders(
                vertexShader,
                fragmentShader
            )
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
    override val endTime = note.time + note.sustain
    override val derived = true
    override val sortLevel =
        SortLevel.StringTail(note.string.toInt())
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        val parityHandle = glGetAttribLocation(mProgram, "vParity").also {
            glEnableVertexAttribArray(it)
            glVertexAttribPointer(it, 1, GL_FLOAT, false, 4, parityBuffer)
        }
        glGetUniformLocation(mProgram, "uPosition").also {
            val x = if (event.fret > 0) event.fret - 0.5f
                    else anchor.fret + anchor.width/2f - 1f
            glUniform4f(
                it,
                x,
                1.5f * (event.string + 0.5f) / 6f,
                (time - event.time) * scrollSpeed,
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