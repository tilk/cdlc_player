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
import eu.tilk.cdlcplayer.viewer.Event
import eu.tilk.cdlcplayer.viewer.SortLevel

class NoteMarker(
    private val note : Event.Note,
    private val anchor : Event.Anchor
    ) : EventShape<Event.Note>(vertexCoords, drawOrder, this, note) {
    companion object : StaticShape.StaticCompanionBase(
        floatArrayOf(
            0f, 0f, 0.0f,
            0f, 0f, -0.25f,
            1f, 0f, -0.25f,
            1f, 0f, 0.0f
        ),
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        ),
        """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform float uTime;
            uniform ivec2 uFret;
            in vec4 vPosition;
            out vec2 vTexCoord;
            void main() {
                vec4 actPosition = vec4(float(uFret.x - 1) + vPosition.x * float(uFret.y), vPosition.y, uTime + vPosition.z, vPosition.w);
                gl_Position = uMVPMatrix * actPosition;
                vTexCoord = vec2(vPosition.x, 1.0 + vPosition.z / 0.25);
            }
        """.trimIndent(),
        """
            #version 300 es
            precision mediump float;
            in vec2 vTexCoord;
            out vec4 FragColor;
            $bumpColorGLSL
            void main() {
                FragColor = vec4(bumpColor, min(1.0, vTexCoord.y * 2.0));
            }
        """.trimIndent()
    ) {
        private val uTime = GLUniformCache("uTime")
        private val uFret = GLUniformCache("uFret")
    }

    override val sortLevel = SortLevel.Beat
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glUniform1f(uTime.value, (time - event.time) * scrollSpeed)
        if (note.fret > 0)
            glUniform2i(uFret.value, note.fret.toInt(), 1)
        else
            glUniform2i(uFret.value, anchor.fret.toInt(), anchor.width.toInt())
        super.internalDraw(time, scrollSpeed)
    }
}