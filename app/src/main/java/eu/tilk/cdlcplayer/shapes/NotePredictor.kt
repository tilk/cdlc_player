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
import eu.tilk.cdlcplayer.viewer.NoteInfo
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class NotePredictor(private val notes : Iterable<NoteInfo>) :
    StaticShape(vertexCoords, drawOrder, this)
{
    companion object : StaticCompanionBase(
        floatArrayOf(
            -0.25f, -0.12f, 0.0f,
            -0.25f, 0.12f, 0.0f,
            0.25f, 0.12f, 0.0f,
            0.25f, -0.12f, 0.0f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            in vec3 vPosition;
            in vec3 vOffset;
            in int vString;
            out vec2 vTexCoord;
            flat out int aString;
            void main() {
                gl_Position = uMVPMatrix * 
                    vec4(vPosition / (1.0 + vOffset.z / 10.0) + vec3(vOffset.xy, 0.0), 1.0);
                vTexCoord = vec2(vPosition.x / 0.25, vPosition.y / 0.12);
                aString = vString;
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            in vec2 vTexCoord;
            flat in int aString;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                float dist = max(abs(vTexCoord.x), abs(vTexCoord.y));
                float scaling = min(1.0, max(
                    1.0+(atan(1.0-20.0*abs(dist-0.8)))/3.14,
                    step(dist, 0.8) * (0.85 + vTexCoord.y / 4.0)
                ));
                FragColor = vec4(
                    scaling * stringColors[aString],
                    step(0.65, dist));
            }
        """.trimIndent()
    ) {
        private lateinit var calculator : NoteCalculator
        fun initialize(calculator: NoteCalculator) {
            super.initialize()
            this.calculator = calculator
        }
        private val vString = GLAttribCache("vString")
        private val vOffset = GLAttribCache("vOffset")
    }
    private val stringBuffer : ShortBuffer = ByteBuffer.allocateDirect(notes.count() * 2)
        .run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                notes.forEach {
                    put(it.string.toShort())
                }
                position(0)
            }
        }
    private val offsetBuffer : FloatBuffer =
        ByteBuffer.allocateDirect(notes.count() * COORDS_PER_VERTEX * 4)
            .run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    notes.forEach {
                        put(calculator.calcX(it.fret) + it.slide)
                        put(calculator.calcY(it.string, it.bend))
                        put(it.time)
                    }
                    position(0)
                }
            }
    override val instances : Int = notes.count()
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        vString.value.also {
            glEnableVertexAttribArray(vString.value)
            glVertexAttribDivisor(vString.value, 1)
            glVertexAttribIPointer(
                vString.value,
                1, GL_SHORT,
                2, stringBuffer
            )
        }
        vOffset.value.also {
            glEnableVertexAttribArray(vOffset.value)
            glVertexAttribDivisor(vOffset.value, 1)
            glVertexAttribPointer(
                vOffset.value,
                COORDS_PER_VERTEX, GL_FLOAT, false,
                COORDS_PER_VERTEX * 4, offsetBuffer)
        }
        super.internalDraw(time, scrollSpeed)
        vOffset.value.also {
            glVertexAttribDivisor(it, 0)
            glDisableVertexAttribArray(it)
        }
        vString.value.also {
            glVertexAttribDivisor(it, 0)
            glDisableVertexAttribArray(it)
        }
    }
}