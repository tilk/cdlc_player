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
import kotlin.math.max

class Anchor(
    anchor : Event.Anchor
) : EventShape<Event.Anchor>(vertexCoords, drawOrder, this, anchor) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            -0.5f, 0.0f, 0.0f,
            -0.5f, 0.0f, -1.0f,
            24.5f, 0.0f, -1.0f,
            24.5f, 0.0f, 0.0f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
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
        """.trimIndent(),
        """
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
                int fret = int(pos) + 1;
                float coef = isSpecialFret(fret) ? 0.7 : 1.0;
                FragColor = vec4(coef * (
                    step(float(uFret.x - 1), pos) * step(pos, float(uFret.x + uFret.y - 1)) * (cos(2.0*fdist)+1.0)/2.0 * beltColor
                    + (tanh(20.0*(fdist-0.95))+1.0)/2.0 * bumpColor), 1.0);
            }
        """.trimIndent()
    ) {
        private val uTime = GLUniformCache("uTime")
        private val uFret = GLUniformCache("uFret")
    }
    override val sortLevel = SortLevel.Tab
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        val movedTime = max(event.time, time)
        glUniform2f(uTime.value,
            (time - movedTime) * scrollSpeed,
            (event.endTime - movedTime) * scrollSpeed)
        glUniform2i(uFret.value, event.fret.toInt(), event.width.toInt())
        super.internalDraw(time, scrollSpeed)
    }
}