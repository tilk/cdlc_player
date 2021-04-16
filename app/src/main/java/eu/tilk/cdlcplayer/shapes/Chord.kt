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
import eu.tilk.cdlcplayer.viewer.Effect

class Chord(
    chord : Event.Chord,
    private val anchor : Event.Anchor,
    private val string : Int,
    private val repeated : Boolean
) : EventShape<Event.Chord>(vertexCoords, drawOrder, this, chord) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform float uTime;
            uniform int uString;
            uniform ivec2 uFret;
            uniform float uMult;
            in vec4 vPosition;
            out vec2 vTexCoord;
            out float zPos;
            void main() {
                vec2 qPosition = vec2(vPosition.x, (float(uString + 1) + vPosition.y) * 0.25 / 2.0);
                vec4 actPosition = vec4(
                    float(uFret.x-1) + qPosition.x * float(uFret.y), 
                    qPosition.y * 2.0, vPosition.z + uTime, vPosition.w);
                gl_Position = uMVPMatrix * actPosition;
                zPos = actPosition.z;
                vTexCoord = vec2(2.0, uMult) * qPosition - vec2(1.0, 1.0);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            uniform int uEffect;
            in vec2 vTexCoord;
            in float zPos;
            out vec4 FragColor;
            $beltColorGLSL
            $bumpColorGLSL
            $logisticGLSL
            void main() {
                float dist = max(abs(vTexCoord.x), abs(vTexCoord.y));
                float tcox = uEffect == ${Effect.FrethandMute.ordinal} ? vTexCoord.x * 2.0 : vTexCoord.x; 
                float xdist = min(abs(tcox - vTexCoord.y),
                                  abs(tcox + vTexCoord.y));
                float coef = logistic(5.0 * (dist - 0.8));
                float effc = uEffect >= 0 ? max(step(0.9, dist), step(0.2, xdist)) : 1.0;
                FragColor = vec4(effc * (coef * bumpColor + (1.0 - coef) * beltColor), 
                                 (0.2 + coef * 0.8) * $fogGLSL);
            }
        """.trimIndent()
    ) {
        private val uTime = GLUniformCache("uTime")
        private val uMult = GLUniformCache("uMult")
        private val uString = GLUniformCache("uString")
        private val uFret = GLUniformCache("uFret")
        private val uEffect = GLUniformCache("uEffect")
    }

    override val sortLevel = SortLevel.ChordBox(string)
    override val derived = string > 0
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glUniform1f(uTime.value, (time - event.time) * scrollSpeed)
        glUniform1f(uMult.value, if (repeated) 4.0f else 2.0f)
        glUniform1i(uString.value, string)
        glUniform2i(uFret.value, anchor.fret.toInt(), anchor.width.toInt())
        glUniform1i(uEffect.value, if (repeated) event.effect?.ordinal ?: -1 else -1)
        super.internalDraw(time, scrollSpeed)
    }
}