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

class Chord(
    chord : Event.Chord,
    private val anchor : Event.Anchor
) : EventShape<Event.Chord>(vertexCoords, drawOrder, mProgram, chord) {
    companion object {
        private val vertexCoords = floatArrayOf(
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform float uTime;
            uniform ivec2 uFret;
            in vec4 vPosition;
            out vec2 vTexCoord;
            void main() {
                vec4 actPosition = vec4(
                    float(uFret.x-1) + vPosition.x * float(uFret.y), 
                    vPosition.y * 1.5, vPosition.z + uTime, vPosition.w);
                gl_Position = uMVPMatrix * actPosition;
                vTexCoord = vPosition.xy;
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            in vec2 vTexCoord;
            out vec4 FragColor;
            void main() {
                FragColor = vec4(0.2, 0.4, 0.2, 0.3);
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

    override val sortLevel = SortLevel.Chord
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glGetUniformLocation(mProgram, "uTime").also {
            glUniform1f(it, (time - event.time) * scrollSpeed)
        }
        glGetUniformLocation(mProgram, "uFret").also {
            glUniform2i(it, anchor.fret.toInt(), anchor.width.toInt())
        }
        super.internalDraw(time, scrollSpeed)
    }
}