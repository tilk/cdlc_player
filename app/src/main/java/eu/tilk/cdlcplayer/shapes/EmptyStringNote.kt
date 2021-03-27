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
import eu.tilk.cdlcplayer.viewer.Textures

class EmptyStringNote(
    note : Event.Note,
    override val derived : Boolean,
    private val anchor : Event.Anchor
) : EventShape<Event.Note>(vertexCoords, drawOrder, this, note) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            0f, -0.24f, 0.0f,
            0f, 0.24f, 0.0f,
            1f, 0.24f, 0.0f,
            1f, -0.24f, 0.0f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform vec4 uPosition;
            uniform int uWidth;
            uniform int uEffect;
            in vec4 vPosition;
            out vec2 vTexCoord;
            out vec2 aTexCoord;
            void main() {
                vec4 rPosition = vec4(vPosition.x * float(uWidth), vPosition.y, vPosition.z, vPosition.w); 
                gl_Position = uMVPMatrix * (rPosition + uPosition);
                vTexCoord = vec2((vPosition.x - 0.5) * float(uWidth), vPosition.y / 0.12);
                int ex = uEffect / 5;
                int ey = uEffect % 5;
                vec2 inTexCoord = vec2(
                    (vTexCoord.x * float(uWidth) / 2.0) + 0.5,
                    (vTexCoord.y + 2.0) / 4.0
                );
                aTexCoord = step(1.0, float(uEffect)) * (
                    inTexCoord / vec2(2.0, -5.0) 
                    + vec2(float(ex) / 2.0, float(ey) / 5.0 + 0.2)
                );
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            uniform sampler2D uTexture;
            uniform int uString;
            in vec2 vTexCoord;
            in vec2 aTexCoord;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                float zer = step(-0.5, vTexCoord.x) * step(vTexCoord.x, 0.5);
                vec4 effColor = zer * texture(uTexture, aTexCoord);
                vec3 aEffColor = effColor.r * stringColors[uString] 
                    + effColor.g * vec3(1.0, 1.0, 1.0);
                float scl = (tanh(-abs(vTexCoord.y*10.0)+3.0) + 1.0) / 2.0;
                FragColor = vec4(aEffColor + (1.0 - effColor.a) * stringColors[uString], 
                    max(effColor.a, scl));
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
        private val uWidth    = GLUniformCache("uWidth")
        private val uString   = GLUniformCache("uString")
        private val uTexture  = GLUniformCache("uTexture")
        private val uEffect   = GLUniformCache("uEffect")
    }
    override val endTime : Float = event.time
    override val sortLevel =
        SortLevel.String(calculator.sort(note.string))
    override fun internalDraw(time: Float, scrollSpeed : Float) {
        glUniform4f(uPosition.value,
            anchor.fret - 1f,
            calculator.calcY(event.string),
            calculator.calcZ(event.time, time, scrollSpeed),
            0f)
        glUniform1i(uWidth.value, anchor.width.toInt())
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