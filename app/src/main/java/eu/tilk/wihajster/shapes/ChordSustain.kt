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
import android.opengl.GLES31.*
import eu.tilk.wihajster.viewer.SortLevel

class ChordSustain(private val chord : Event.HandShape,
                   private val anchor : Event.Anchor) :
    EventShape<Event.HandShape>(vertexCoords, drawOrder, mProgram, chord) {
    companion object {
        private val vertexCoords = floatArrayOf(
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private var mProgram : Int = -1
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform float uTime;
            uniform float uSustain;
            uniform ivec2 uFret;
            in vec4 vPosition;
            out float xCoord;
            out float zPos;
            void main() {
                xCoord = vPosition.x * float(uFret.y + 2) - 1.0;
                vec4 actPosition = vec4(
                    float(uFret.x-1) + xCoord,
                    vPosition.y,
                    vPosition.z * uSustain + uTime,
                    vPosition.w);
                zPos = actPosition.z;
                gl_Position = uMVPMatrix * actPosition;
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            uniform ivec2 uFret;
            in float xCoord;
            in float zPos;
            out vec4 FragColor;
            $bumpColorGLSL
            $logisticGLSL
            float col_fun(float a) {
                return logistic(0.7 - 8.0*a);
            }
            void main() {
                FragColor = vec4(bumpColor, step(zPos, 0.0) * max(
                    0.2 * step(0.0, xCoord) * step(xCoord, float(uFret.y)), max(
                    col_fun(abs(xCoord)), 
                    col_fun(abs(xCoord - float(uFret.y))))));
            }
        """.trimIndent()
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
    override val sortLevel = SortLevel.Beat
    override val endTime = chord.time + chord.sustain
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glGetUniformLocation(mProgram, "uTime").also {
            glUniform1f(it, (time - event.time) * scrollSpeed)
        }
        glGetUniformLocation(mProgram, "uSustain").also {
            glUniform1f(it, chord.sustain * scrollSpeed)
        }
        glGetUniformLocation(mProgram, "uFret").also {
            glUniform2i(it, anchor.fret.toInt(), anchor.width.toInt())
        }
        super.internalDraw(time, scrollSpeed)
    }
}