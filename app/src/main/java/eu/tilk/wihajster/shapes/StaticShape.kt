package eu.tilk.wihajster.shapes

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

abstract class StaticShape(
    vertexCoords : FloatArray,
    drawOrder : ShortArray,
    mProgram : Int
) : Shape(mProgram) {
    override val vertexBuffer : FloatBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4)
        .run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertexCoords)
                position(0)
            }
        }
    override val drawListBuffer : ShortBuffer = ByteBuffer.allocateDirect(drawOrder.size * 2)
        .run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }
    override val drawListSize = drawOrder.size
}