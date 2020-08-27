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
import java.nio.FloatBuffer
import java.nio.ShortBuffer

abstract class Shape(
    private val mProgram : Int
) {
    protected abstract val vertexBuffer : FloatBuffer
    protected abstract val drawListBuffer : ShortBuffer
    protected abstract val drawListSize : Int
    protected open val instances : Int = 1
    protected open fun internalDraw(time: Float, scrollSpeed : Float) {
        val positionHandle = glGetAttribLocation(mProgram, "vPosition")
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle,
            COORDS_PER_VERTEX, GL_FLOAT, false,
            COORDS_PER_VERTEX * 4, vertexBuffer)
        if (instances > 1)
            glDrawElementsInstanced(GL_TRIANGLES, drawListSize, GL_UNSIGNED_SHORT,
                drawListBuffer, instances)
        else
            glDrawElements(GL_TRIANGLES, drawListSize, GL_UNSIGNED_SHORT, drawListBuffer)
        glDisableVertexAttribArray(positionHandle)
    }
    private fun prepare(mvpMatrix : FloatArray) {
        glUseProgram(mProgram)
        val vPMatrixHandle = glGetUniformLocation(mProgram, "uMVPMatrix")
        glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)
    }
    fun draw(mvpMatrix : FloatArray, time: Float, scrollSpeed : Float) {
        prepare(mvpMatrix)
        internalDraw(time, scrollSpeed)
    }
}