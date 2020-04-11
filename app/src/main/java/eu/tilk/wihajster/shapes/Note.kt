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

class Note(note : Event.Note) : EventShape<Event.Note>(vertexCoords, drawOrder, mProgram, note) {
    companion object {
        private val vertexCoords = floatArrayOf(
            -0.25f, -0.12f, 0.0f,
            -0.25f, 0.12f, 0.0f,
            0.25f, 0.12f, 0.0f,
            0.25f, -0.12f, 0.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform vec4 uPosition;
            in vec4 vPosition;
            out vec2 vTexCoord;
            void main() {
                gl_Position = uMVPMatrix * (vPosition + uPosition);
                vTexCoord = vec2(vPosition.x / 0.25, vPosition.y / 0.12);
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            uniform int uString;
            in vec2 vTexCoord;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                float dist = max(abs(vTexCoord.x), abs(vTexCoord.y));
                float scaling = min(1.0, max(
                    1.0+(atan(1.0-20.0*abs(dist-0.8)))/3.14,
                    step(dist, 0.8) * (0.85 + vTexCoord.y / 4.0)
                ));
                FragColor = vec4(scaling * stringColors[uString], 1.0);
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
    override val sortLevel =
        SortLevel.String(note.string.toInt())
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        val positionHandle = glGetUniformLocation(mProgram, "uPosition")
        glUniform4f(positionHandle,
            event.fret - 0.5f,
            1.5f * (event.string + 0.5f) / 6f,
            (time - event.time) * scrollSpeed,
            0f)
        val stringHandle = glGetUniformLocation(mProgram, "uString")
        glUniform1i(stringHandle, event.string.toInt())
        super.internalDraw(time, scrollSpeed)
    }
}