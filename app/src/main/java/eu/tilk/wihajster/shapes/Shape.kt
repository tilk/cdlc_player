package eu.tilk.wihajster.shapes

import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.opengl.GLES31.*

abstract class Shape(
    private val vertexCoords : FloatArray,
    private val drawOrder : ShortArray,
    private val mProgram : Int
) {
    private val vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4)
        .run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(vertexCoords)
            position(0)
        }
    }
    private val drawListBuffer = ByteBuffer.allocateDirect(drawOrder.size * 2)
        .run {
        order(ByteOrder.nativeOrder())
        asShortBuffer().apply {
            put(drawOrder)
            position(0)
        }
    }
    protected open fun internalDraw(time: Float, scrollSpeed : Float) {
        val positionHandle = glGetAttribLocation(mProgram, "vPosition")
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle,
            COORDS_PER_VERTEX, GL_FLOAT, false,
            COORDS_PER_VERTEX * 4, vertexBuffer)
        glDrawElements(GL_TRIANGLES, drawOrder.size, GL_UNSIGNED_SHORT, drawListBuffer)
        glDisableVertexAttribArray(positionHandle)
    }
    protected fun prepare(mvpMatrix : FloatArray) {
        glUseProgram(mProgram)
        val vPMatrixHandle = glGetUniformLocation(mProgram, "uMVPMatrix")
        glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)
    }
}