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
import eu.tilk.cdlcplayer.viewer.Event
import eu.tilk.cdlcplayer.viewer.SortLevel
import eu.tilk.cdlcplayer.viewer.Textures

class ChordInfo(chord : Event.Chord,
                private val anchor : Event.Anchor) :
    EventShape<Event.Chord>(vertexCoords, drawOrder, this, chord) {
    companion object : StaticCompanionBase(
        floatArrayOf(
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.5f, 0.0f,
            0.0f, 0.5f, 0.0f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform float uTime;
            uniform ivec2 uFret;
            uniform ivec2 uChord;
            in vec4 vPosition;
            out vec2 vTexCoord;
            out float zPos;
            void main() {
                vec4 actPosition = vec4(
                    float(uFret.x-1) + vPosition.x * float(uFret.y), 
                    vPosition.y + 2.0, vPosition.z + uTime, vPosition.w);
                gl_Position = uMVPMatrix * actPosition;
                zPos = actPosition.z;
                vTexCoord = vec2(vPosition.x, 
                    (float(uChord.x) + 1.0 - vPosition.y / 0.5) / float(uChord.y));
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            uniform sampler2D uTexture;
            in vec2 vTexCoord;
            in float zPos;
            out vec4 FragColor;
            void main() {
                vec4 texColor = texture(uTexture, vTexCoord); 
                FragColor = vec4(texColor.rgb, texColor.a * $fogGLSL);
            }
        """.trimIndent()
    ) {
        private lateinit var textures : Textures
        private var chordCount : Int = -1
        fun initialize(textures : Textures, chordCount : Int) {
            super.initialize()
            this.textures = textures
            this.chordCount = chordCount
        }
        private val uTime    = GLUniformCache("uTime")
        private val uChord   = GLUniformCache("uChord")
        private val uTexture = GLUniformCache("uTexture")
        private val uFret    = GLUniformCache("uFret")
    }

    override val sortLevel = SortLevel.Chord
    override val derived = true
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glUniform1f(uTime.value, (time - event.time) * scrollSpeed)
        glUniform2i(uChord.value, event.id, chordCount)
        glUniform1i(uTexture.value, 0)
        glUniform2i(uFret.value, anchor.fret.toInt(), anchor.width.toInt())
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textures.chordTextures)
        super.internalDraw(time, scrollSpeed)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}