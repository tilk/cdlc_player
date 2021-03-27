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

import android.opengl.GLES31.*
import eu.tilk.cdlcplayer.shapes.utils.NoteCalculator
import eu.tilk.cdlcplayer.viewer.FingerInfo
import eu.tilk.cdlcplayer.viewer.Textures

class Finger(private val finger : FingerInfo)
    : StaticShape(vertexCoords, drawOrder, mProgram) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            -0.25f, -0.125f, 0.0f,
            -0.25f, 0.125f, 0.0f,
            0.25f, 0.125f, 0.0f,
            0.25f, -0.125f, 0.0f
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
                vTexCoord = vec2(vPosition.x / 0.25 / 2.0 + 0.5, -vPosition.y / 0.125 / 2.0 + 0.5);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            uniform sampler2D uTexture;
            uniform int uFinger;
            in vec2 vTexCoord;
            out vec4 FragColor;
            void main() {
                float x = vTexCoord.x;
                float y = vTexCoord.y;
                FragColor = texture(uTexture, vec2(x / 6.0, (y + float(uFinger - 1)) / 12.0));
            }
        """.trimIndent()
    ) {
        private lateinit var textures : Textures
        private lateinit var calculator : NoteCalculator
        fun initialize(textures : Textures, calculator: NoteCalculator) {
            super.initialize()
            this.textures = textures
            this.calculator = calculator
        }
    }
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glGetUniformLocation(mProgram, "uPosition").also {
            glUniform4f(
                it,
                calculator.calcX(finger.fret),
                calculator.calcY(finger.string),
                0f,
                0f
            )
        }
        glGetUniformLocation(mProgram, "uFinger").also {
            glUniform1i(it, finger.finger.toInt())
        }
        glGetUniformLocation(mProgram, "uTexture").also {
            glUniform1i(it, 0)
        }
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textures.fretNumbers)
        super.internalDraw(time, scrollSpeed)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}