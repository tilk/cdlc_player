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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

abstract class StaticShape(
    vertexCoords : FloatArray,
    drawOrder : ShortArray,
    companion : CompanionBase
) : Shape(companion) {
    open class StaticCompanionBase(
        protected val vertexCoords : FloatArray,
        protected val drawOrder : ShortArray,
        vertexShaderCode : String,
        fragmentShaderCode : String
    ) : CompanionBase(vertexShaderCode, fragmentShaderCode)
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