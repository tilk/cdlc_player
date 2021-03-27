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
import eu.tilk.cdlcplayer.shapes.utils.NoteCalculator
import eu.tilk.cdlcplayer.viewer.NoteInfo
import eu.tilk.cdlcplayer.viewer.Textures

class Note(note : Event.Note, override val derived : Boolean = false) :
    NoteyShape<Event.Note>(vertexCoords, drawOrder, this, note) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            -0.5f, -0.24f, 0.0f,
            -0.5f, 0.24f, 0.0f,
            0.5f, 0.24f, 0.0f,
            0.5f, -0.24f, 0.0f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
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
        """.trimIndent(),
        """
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
    ) {
        private lateinit var textures : Textures
        private lateinit var calculator : NoteCalculator
        fun initialize(textures : Textures, calculator : NoteCalculator) {
            super.initialize()
            this.textures = textures
            this.calculator = calculator
        }
        private val uPosition = GLUniformCache("uPosition")
        private val uString   = GLUniformCache("uString")
        private val uTexture  = GLUniformCache("uTexture")
        private val uEffect   = GLUniformCache("uEffect")
    }

    override val endTime : Float = event.time

    override val sortLevel =
        SortLevel.String(calculator.sort(note.string))

    override fun noteInfo(time: Float, scrollSpeed : Float) : NoteInfo? =
        if (event.time > time)
            NoteInfo((event.time - time) * scrollSpeed, event.fret, event.string,
                event.bendValue(0f), 0f)
        else
            null

    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glUniform4f(
            uPosition.value,
            calculator.calcX(event.fret),
            calculator.calcY(event.string, event.bendValue(0f)),
            calculator.calcZ(event.time, time, scrollSpeed),
            0f
        )
        glUniform1i(uString.value, event.string.toInt())
        glUniform1i(uTexture.value, 0)
        glUniform1i(uEffect.value, event.effect?.ordinal ?: -1)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textures.effects)
        super.internalDraw(time, scrollSpeed)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}