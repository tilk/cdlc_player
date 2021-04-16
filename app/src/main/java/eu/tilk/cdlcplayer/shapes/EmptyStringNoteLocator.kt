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
import eu.tilk.cdlcplayer.shapes.utils.NoteCalculator

class EmptyStringNoteLocator(
    note : Event.Note,
    private val stringHeight : Int,
    private val anchor : Event.Anchor
) : EventShape<Event.Note>(vertexCoords, drawOrder, this, note) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            0f, -0.125f, 0.0f,
            0f, 0.125f, 0.0f,
            1f, 0.125f, 0.0f,
            1f, -0.125f, 0.0f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform vec4 uPosition;
            uniform int uWidth;
            in vec4 vPosition;
            out vec2 vTexCoord;
            out float zPos;
            void main() {
                vec4 rPosition = vec4(vPosition.x * float(uWidth), vPosition.y, vPosition.z, vPosition.w);
                vec4 pos = rPosition + uPosition;
                gl_Position = uMVPMatrix * pos;
                zPos = pos.z;
                vTexCoord = vec2(
                    (vPosition.x - 0.5) / 0.5,
                    (vPosition.y + uPosition.y) / 0.25);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            uniform int uString;
            uniform int uCalcString;
            in vec2 vTexCoord;
            in float zPos;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                float alp = step(0.9, abs(vTexCoord.x)) * max(1.0 - vTexCoord.y / float(uCalcString + 1), 0.0);
                FragColor = vec4(stringColors[uString], alp * $fogGLSL);
            }
        """.trimIndent()
    ) {
        private lateinit var calculator : NoteCalculator
        fun initialize(calculator: NoteCalculator) {
            super.initialize()
            this.calculator = calculator
        }
        private val uPosition   = GLUniformCache("uPosition")
        private val uWidth      = GLUniformCache("uWidth")
        private val uString     = GLUniformCache("uString")
        private val uCalcString = GLUniformCache("uCalcString")
    }
    override val sortLevel = SortLevel.ChordBox(stringHeight)
    override val endTime = event.time
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glUniform4f(
            uPosition.value,
            anchor.fret - 1f,
            calculator.calcY(calculator.sort(stringHeight.toByte()).toByte()),
            calculator.calcZ(event.time, time, scrollSpeed),
            0f
        )
        glUniform1i(uWidth.value, anchor.width.toInt())
        glUniform1i(uString.value, event.string.toInt())
        glUniform1i(uCalcString.value, calculator.sort(event.string))
        super.internalDraw(time, scrollSpeed)
    }
}