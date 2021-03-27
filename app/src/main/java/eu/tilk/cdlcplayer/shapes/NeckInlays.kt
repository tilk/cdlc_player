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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

class NeckInlays(private val leftFret : Int, private val rightFret : Int) :
    StaticShape(vertexCoords, drawOrder, this) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            0f, 0f, 0f,
            0f, 1.5f, 0f,
            1f, 1.5f, 0f,
            1f, 0f, 0f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform ivec2 uFret;
            in vec4 vPosition;
            in int vInlayFret;
            out vec2 vTexCoord;
            flat out int twoDot;
            flat out int actv;
            $specialFretsGLSL
            void main() {
                vec4 actPosition = vec4(
                    vPosition.x + float(vInlayFret - 1), 
                    vPosition.y + 0.25,
                    vPosition.zw); 
                gl_Position = uMVPMatrix * actPosition;
                vTexCoord = vPosition.xy;
                twoDot = int(isTwoDotFret(vInlayFret));
                actv = int(vInlayFret >= uFret.x && vInlayFret < uFret.y);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            in vec2 vTexCoord;
            flat in int twoDot;
            flat in int actv;
            out vec4 FragColor;
            $laneColorsGLSL
            float icirc(vec2 cen) {
                float dist = distance(vTexCoord, cen) / 0.1;
                return step(dist, 1.0) * max(dist * dist, 0.2);
            }
            void main() {
                float coef = max(
                    icirc(vec2(0.5, 0.75 - float(twoDot) * 2.0 * 0.25)),
                    icirc(vec2(0.5, 0.75 + float(twoDot) * 2.0 * 0.25))
                );
                vec3 col = laneColors[actv];
                FragColor = vec4(col, coef * 0.7);
            }
        """.trimIndent()
    ) {
        private val inlayFrets = shortArrayOf(
            3, 5, 7, 9, 12, 15, 17, 19, 21, 24
        )
        private val uFret = GLUniformCache("uFret")
        private val vInlayFret = GLAttribCache("vInlayFret")
    }
    private val inlayFretsBuffer : ShortBuffer = ByteBuffer.allocateDirect(inlayFrets.size * 2)
        .run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(inlayFrets)
                position(0)
            }
        }
    override val instances = inlayFrets.size
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        vInlayFret.value.also {
            glEnableVertexAttribArray(it)
            glVertexAttribDivisor(it, 1)
            glVertexAttribIPointer(
                it,
                1, GL_SHORT,
                2, inlayFretsBuffer
            )
        }
        glUniform2i(uFret.value, leftFret, rightFret)
        super.internalDraw(time, scrollSpeed)
        vInlayFret.value.also {
            glVertexAttribDivisor(it, 0)
            glDisableVertexAttribArray(it)
        }
    }
}