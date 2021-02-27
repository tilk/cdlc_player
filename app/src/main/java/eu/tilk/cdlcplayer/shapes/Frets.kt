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
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Frets : StaticShape(vertexCoords, drawOrder, mProgram) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            -0.04f, 0f, 0f,
            -0.04f, 1.5f, 0f,
            0.04f, 1.5f, 0f,
            0.04f, 0f, 0f,
            -0.015f, 0.05f, 0.05f,
            -0.015f, 1.45f, 0.05f,
            0.015f, 1.45f, 0.05f,
            0.015f, 0.05f, 0.05f
        ),
        shortArrayOf(
            0, 1, 5,
            0, 5, 4,
            7, 6, 2,
            7, 2, 3,
            5, 1, 2,
            5, 2, 6,
            0, 4, 7,
            0, 7, 3,
            4, 5, 6,
            4, 6, 7
        ),
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            in vec4 vPosition;
            in vec3 vNormal;
            in int vFret;
            out vec3 normal;
            void main() {
                vec4 actPosition = vec4(
                    vPosition.x + float(vFret - 1), 
                    vPosition.y + 0.25, 
                    vPosition.zw); 
                gl_Position = uMVPMatrix * actPosition;
                normal = normalize(vNormal);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            uniform vec3 uLight;
            in vec3 normal;
            out vec4 FragColor;
            void main() {
                float diff = max(dot(normalize(normal), normalize(uLight)), 0.0);
                vec3 col = vec3(0.84, 0.52, 0.3);
                FragColor = vec4(diff * col, 1.0);
            }
        """.trimIndent()
    ) {
        private val normals = floatArrayOf(
            -0.5f, -0.1f, 0.5f,
            -0.5f, 0.1f, 0.5f,
            0.5f, 0.1f, 0.5f,
            0.5f, -0.1f, 0.5f,
            -0.3f, 0f, 1f,
            -0.3f, 0f, 1f,
            0.3f, 0f, 1f,
            0.3f, 0f, 1f
        )
        private val frets = (1..24).toList().map { it.toShort() }.toShortArray()
    }
    override val instances = frets.size
    private val fretsBuffer : ShortBuffer = ByteBuffer.allocateDirect(frets.size * 2)
        .run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(frets)
                position(0)
            }
        }
    private val normalsBuffer : FloatBuffer = ByteBuffer.allocateDirect(normals.size * 4)
        .run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(normals)
                position(0)
            }
        }
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        val normalHandle = glGetAttribLocation(mProgram, "vNormal")
        glEnableVertexAttribArray(normalHandle)
        glVertexAttribPointer(normalHandle,
            COORDS_PER_VERTEX, GL_FLOAT, false,
            COORDS_PER_VERTEX * 4, normalsBuffer)
        val fretHandle = glGetAttribLocation(mProgram, "vFret")
        glEnableVertexAttribArray(fretHandle)
        glVertexAttribDivisor(fretHandle, 1)
        glVertexAttribIPointer(
            fretHandle,
            1, GL_SHORT,
            2, fretsBuffer
        )
        glGetUniformLocation(mProgram, "uLight").also {
            glUniform3f(it, 0.5f, 0.1f, 1f)
        }
        super.internalDraw(time, scrollSpeed)
        glVertexAttribDivisor(fretHandle, 0)
        glDisableVertexAttribArray(fretHandle)
        glDisableVertexAttribArray(normalHandle)
    }
}