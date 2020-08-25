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

class EmptyStringNote(
    note : Event.Note,
    override val derived : Boolean,
    private val anchor : Event.Anchor
) : EventShape<Event.Note>(vertexCoords, drawOrder, mProgram, note) {
    companion object {
        private val vertexCoords = floatArrayOf(
            0f, -0.12f, 0.0f,
            0f, 0.12f, 0.0f,
            1f, 0.12f, 0.0f,
            1f, -0.12f, 0.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform vec4 uPosition;
            uniform int uWidth;
            in vec4 vPosition;
            out vec2 vTexCoord;
            void main() {
                vec4 rPosition = vec4(vPosition.x * float(uWidth), vPosition.y, vPosition.z, vPosition.w); 
                gl_Position = uMVPMatrix * (rPosition + uPosition);
                vTexCoord = vec2((vPosition.x - 0.5) / 0.5, vPosition.y / 0.12);
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
                float scl = (tanh(-abs(vTexCoord.y*10.0)+3.0) + 1.0) / 2.0;
                FragColor = vec4(stringColors[uString], scl);
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
    override fun internalDraw(time: Float, scrollSpeed : Float) {
        val positionHandle = glGetUniformLocation(mProgram, "uPosition")
        glUniform4f(positionHandle,
            anchor.fret - 1f,
            1.5f * (event.string + 0.5f) / 6f,
            (time - event.time) * scrollSpeed,
            0f)
        val fretHandle = glGetUniformLocation(mProgram, "uWidth")
        glUniform1i(fretHandle, anchor.width.toInt())
        val stringHandle = glGetUniformLocation(mProgram, "uString")
        glUniform1i(stringHandle, event.string.toInt())
        super.internalDraw(time, scrollSpeed)
    }
}