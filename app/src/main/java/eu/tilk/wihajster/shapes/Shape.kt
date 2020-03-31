package eu.tilk.wihajster.shapes

import android.opengl.GLES31.*
import java.nio.FloatBuffer
import java.nio.ShortBuffer

abstract class Shape(
    private val mProgram : Int
) {
    protected abstract val vertexBuffer : FloatBuffer
    protected abstract val drawListBuffer : ShortBuffer
    protected abstract val drawListSize : Int
    protected open fun internalDraw(time: Float, scrollSpeed : Float) {
        val positionHandle = glGetAttribLocation(mProgram, "vPosition")
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle,
            COORDS_PER_VERTEX, GL_FLOAT, false,
            COORDS_PER_VERTEX * 4, vertexBuffer)
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