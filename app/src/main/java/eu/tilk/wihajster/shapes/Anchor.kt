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
import kotlin.math.max

class Anchor(
    anchor : Event.Anchor,
    var lastAnchorTime : Float
) : EventShape<Event.Anchor>(vertexCoords, drawOrder, mProgram, anchor) {
    companion object {
        private val vertexCoords = floatArrayOf(
            -0.5f, 0.0f, 0.0f,
            -0.5f, 0.0f, -1.0f,
            24.5f, 0.0f, -1.0f,
            24.5f, 0.0f, 0.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            in vec4 vPosition;
            uniform vec2 uTime;
            out float pos;
            void main() {
                vec4 actPosition = vec4(vPosition.x, vPosition.y, uTime.x + vPosition.z * uTime.y, vPosition.w);
                gl_Position = uMVPMatrix * actPosition;
                pos = vPosition.x;
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            uniform ivec2 uFret;
            in float pos;
            out vec4 FragColor;
            $beltColorGLSL
            $bumpColorGLSL
            $specialFretsGLSL
            void main() {
                float fdist = 2.0 * distance(fract(pos), 0.5);
                float cdist = 1.0 - fdist;
                int fret = int(pos) + uFret.x;
                float coef = isSpecialFret(fret) ? 0.7 : 1.0;
                FragColor = vec4(coef * (
                    step(float(uFret.x - 1), pos) * step(pos, float(uFret.x + uFret.y - 1)) * (cos(2.0*fdist)+1.0)/2.0 * beltColor
                    + (tanh(20.0*(fdist-0.95))+1.0)/2.0 * bumpColor), 1.0);
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
    override val endTime : Float get() = lastAnchorTime
    override val sortLevel = SortLevel.Tab
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        val timeHandle = glGetUniformLocation(mProgram, "uTime")
        val movedTime = max(event.time, time)
        glUniform2f(timeHandle,
            (time - movedTime) * scrollSpeed,
            (lastAnchorTime - movedTime) * scrollSpeed)
        val fretHandle = glGetUniformLocation(mProgram, "uFret")
        glUniform2i(fretHandle, event.fret.toInt(), event.width.toInt())
        super.internalDraw(time, scrollSpeed)
    }
}