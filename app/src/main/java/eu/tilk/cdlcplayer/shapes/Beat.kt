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
import androidx.lifecycle.LiveData
import eu.tilk.cdlcplayer.viewer.RepeaterInfo

class Beat(
    beat : Event.Beat,
    private val anchor : Event.Anchor,
    private val repeaterInfo : LiveData<RepeaterInfo>
) : EventShape<Event.Beat>(vertexCoords, drawOrder, this, beat) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            0f, 0f, 0.3f,
            0f, 0f, -0.3f,
            1f, 0f, -0.3f,
            1f, 0f, 0.3f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform float uTime;
            uniform ivec2 uFret;
            in vec4 vPosition;
            out vec2 vTexCoord;
            void main() {
                vec4 actPosition = vec4(float(uFret.x - 1) + vPosition.x * float(uFret.y), vPosition.y, uTime + vPosition.z, vPosition.w);
                gl_Position = uMVPMatrix * actPosition;
                vTexCoord = vec2(2.0 * vPosition.x - 1.0, vPosition.z / 0.3);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            in vec2 vTexCoord;
            out vec4 FragColor;
            uniform int uMeasure;
            uniform int uRepeat;
            $bumpColorGLSL
            $repeaterColorGLSL
            void main() {
                bool inTriangle = uRepeat > 0 
                    && (uRepeat == 1 ? vTexCoord.y <= 0.0 : vTexCoord.y >= 0.0)
                    && (abs(vTexCoord.x) + abs(vTexCoord.y) <= 1.0);
                bool inBar = vTexCoord.y <= 0.0 && vTexCoord.y >= -0.5;
                FragColor = vec4(inTriangle ? repeaterColor : bumpColor,
                    inTriangle ? 1.0 : inBar ? 0.5 + float(uMeasure) / 2.0 : 0.0);
            }
        """.trimIndent()
    ) {
        private val uTime = GLUniformCache("uTime")
        private val uFret = GLUniformCache("uFret")
        private val uRepeat = GLUniformCache("uRepeat")
        private val uMeasure = GLUniformCache("uMeasure")
    }

    override val sortLevel = SortLevel.Beat
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glUniform1f(uTime.value, (time - event.time) * scrollSpeed)
        glUniform2i(uFret.value, anchor.fret.toInt(), anchor.width.toInt())
        glUniform1i(uMeasure.value, if (event.measure >= 0) 1 else 0)
        glUniform1i(uRepeat.value, when (this.event) {
            repeaterInfo.value?.startBeat -> 1
            repeaterInfo.value?.endBeat -> 2
            else -> 0
        })
        super.internalDraw(time, scrollSpeed)
    }
}