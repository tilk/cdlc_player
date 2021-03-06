/*
 *     Copyright (C) 2021  Marek Materzok
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

class NoteLocator(
    note : Event.Note,
    private val string : Int
) : EventShape<Event.Note>(vertexCoords, drawOrder, mProgram, note) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            -0.5f, -0.125f, 0.0f,
            -0.5f, 0.125f, 0.0f,
            0.5f, 0.125f, 0.0f,
            0.5f, -0.125f, 0.0f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform vec4 uPosition;
            in vec4 vPosition;
            out vec2 vTexCoord;
            void main() {
                gl_Position = uMVPMatrix * (vPosition + uPosition);
                vTexCoord = vec2(
                    vPosition.x / 0.5,
                    (vPosition.y + uPosition.y) / 0.25);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            uniform mat4 uMVPMatrix;
            uniform int uString;
            in vec2 vTexCoord;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                float alp = step(abs(vTexCoord.x), 0.1) * max(1.0 - vTexCoord.y / float(uString + 1), 0.0);
                FragColor = vec4(stringColors[uString], alp);
            }
        """.trimIndent()
    ) {
        private lateinit var calculator : NoteCalculator
        fun initialize(calculator: NoteCalculator) {
            super.initialize()
            this.calculator = calculator
        }
    }
    override val sortLevel = SortLevel.ChordBox(calculator.sort(string))
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glGetUniformLocation(mProgram, "uPosition").also {
            glUniform4f(
                it,
                calculator.calcX(event.fret),
                calculator.calcY(event.string),
                calculator.calcZ(event.time, time, scrollSpeed),
                0f
            )
        }
        glGetUniformLocation(mProgram, "uString").also {
            glUniform1i(it, event.string.toInt())
        }
        super.internalDraw(time, scrollSpeed)
    }
}