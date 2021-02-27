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

import android.opengl.GLES31.*

class Neck(private val activeStrings : Int) : StaticShape(vertexCoords, drawOrder, mProgram) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            0.0f, 0f, 0.0f,
            0.0f, 1.5f, 0.0f,
            24.0f, 1.5f, 0.0f,
            24.0f, 0f, 0.0f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            in vec4 vPosition;
            out vec2 vTexCoord;
            void main() {
                gl_Position = uMVPMatrix * (vPosition + vec4(0.0, 0.25, 0.0, 0.0));
                vTexCoord = vec2(vPosition.x, vPosition.y / 1.5);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            uniform int uStrings;
            in vec2 vTexCoord;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                float y = vTexCoord.y * 6.0;
                lowp int str = int(y);
                float dist = abs(y - float(str) - 0.5);
                float scl = (1.5+atan(20.0*(dist-0.1)))/3.0;
                float coef = (1.0 + float((uStrings & (1 << str)) != 0)) / 2.0;
                FragColor = vec4(coef * stringColors[str], 1.0 - scl);
            }
        """.trimIndent()
    )
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glGetUniformLocation(mProgram, "uStrings").also {
            glUniform1i(it, activeStrings)
        }
        super.internalDraw(time, scrollSpeed)
    }
}