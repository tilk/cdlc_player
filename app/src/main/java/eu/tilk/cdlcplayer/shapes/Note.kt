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
import eu.tilk.cdlcplayer.viewer.Textures

class Note(note : Event.Note, override val derived : Boolean = false) :
    EventShape<Event.Note>(vertexCoords, drawOrder, mProgram, note) {
    companion object {
        private val vertexCoords = floatArrayOf(
            -0.5f, -0.24f, 0.0f,
            -0.5f, 0.24f, 0.0f,
            0.5f, 0.24f, 0.0f,
            0.5f, -0.24f, 0.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform vec4 uPosition;
            uniform int uEffect;
            in vec4 vPosition;
            out vec2 vTexCoord;
            out vec2 aTexCoord;
            void main() {
                gl_Position = uMVPMatrix * (vPosition + uPosition);
                vTexCoord = vec2(vPosition.x / 0.25, vPosition.y / 0.12);
                int ex = uEffect / 5;
                int ey = uEffect % 5;
                aTexCoord = step(0.0, float(uEffect)) * (
                    (vTexCoord + vec2(2.0, 2.0)) / vec2(4.0 * 2.0, -4.0 * 5.0) 
                    + vec2(float(ex) / 2.0, float(ey) / 5.0 + 0.2)
                );
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            uniform int uString;
            uniform sampler2D uTexture;
            in vec2 vTexCoord;
            in vec2 aTexCoord;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                vec4 effColor = texture(uTexture, aTexCoord);
                vec3 aEffColor = effColor.r * stringColors[uString] 
                    + effColor.g * vec3(1.0, 1.0, 1.0);
                float dist = max(abs(vTexCoord.x), abs(vTexCoord.y));
                float scaling = min(1.0, max(
                    1.0+(atan(1.0-20.0*abs(dist-0.8)))/3.14,
                    step(dist, 0.8) * (0.85 + vTexCoord.y / 4.0)
                ));
                float alp = step(dist, 1.0);
                FragColor = vec4(max(1.0 - alp, effColor.a) * aEffColor 
                        + (1.0 - effColor.a) * scaling * stringColors[uString],
                    max(effColor.a, alp));
            }
        """.trimIndent()
        private var mProgram : Int = -1
        private lateinit var textures : Textures
        fun initialize(textures : Textures) {
            val vertexShader =
                loadShader(
                    GL_VERTEX_SHADER,
                    vertexShaderCode
                )
            val fragmentShader = loadShader(
                GL_FRAGMENT_SHADER,
                fragmentShaderCode
            )
            mProgram = makeProgramFromShaders(
                vertexShader,
                fragmentShader
            )
            this.textures = textures
        }
    }

    override val sortLevel =
        SortLevel.String(note.string.toInt())

    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glGetUniformLocation(mProgram, "uPosition").also {
            glUniform4f(
                it,
                event.fret - 0.5f,
                1.5f * (event.string + 1.5f) / 6f,
                (time - event.time) * scrollSpeed,
                0f
            )
        }
        glGetUniformLocation(mProgram, "uString").also {
            glUniform1i(it, event.string.toInt())
        }
        glGetUniformLocation(mProgram, "uTexture").also {
            glUniform1i(it, 0)
        }
        glGetUniformLocation(mProgram, "uEffect").also {
            glUniform1i(it, event.effect?.ordinal ?: -1)
        }
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textures.effects)
        super.internalDraw(time, scrollSpeed)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}