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

import eu.tilk.cdlcplayer.viewer.Textures
import android.opengl.GLES31.*

class FretNumbers(
    private val textures : Textures,
    private val leftFret : Int,
    private val rightFret : Int
) : StaticShape(vertexCoords, drawOrder, this) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            0.0f, -0.5f, 0.0f,
            0.0f, 0f, 0.0f,
            24.0f, 0f, 0.0f,
            24.0f, -0.5f, 0.0f
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
                vTexCoord = vec2(vPosition.x, -vPosition.y / 0.5);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            uniform sampler2D uTexture;
            uniform ivec2 uFrets;
            in vec2 vTexCoord;
            out vec4 FragColor;
            void main() {
                float x = fract(vTexCoord.x);
                float y = vTexCoord.y;
                lowp int fret = int(vTexCoord.x);
                lowp int col = fret/12;
                if (fret+1 < uFrets.x || fret+1 > uFrets.y-1) col += 2;
                FragColor = texture(uTexture, vec2((x + float(col)) / 6.0, (y + float(fret - 12 * col)) / 12.0));
            }
        """.trimIndent()
    ) {
        private val uFrets   = GLUniformCache("uFrets")
        private val uTexture = GLUniformCache("uTexture")
    }
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glUniform2i(uFrets.value, leftFret, rightFret)
        glUniform1i(uTexture.value, 0)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textures.fretNumbers)
        super.internalDraw(time, scrollSpeed)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}