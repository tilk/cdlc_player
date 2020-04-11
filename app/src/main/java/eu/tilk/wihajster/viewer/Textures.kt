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

package eu.tilk.wihajster.viewer

import android.content.Context
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.opengl.GLES31.*

class Textures(context : Context) {
    val fretNumbers =
        loadTexture(context, "textures/fretNumbers", 9)
    companion object {
        private const val PKM_HEADER_SIZE = 16
        private const val PKM_HEADER_WIDTH_OFFSET = 8
        private const val PKM_HEADER_HEIGHT_OFFSET = 10
        fun loadTexture(context : Context, fileName : String, lastMip : Int) : Int {
            val textures = IntArray(1)
            glGenTextures(1, textures, 0)
            glBindTexture(GL_TEXTURE_2D, textures[0])
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            for (mipLevel in 0..lastMip) {
                val name = fileName + "_mip_" + mipLevel + ".pkm"
                context.resources.assets.open(name).use { input ->
                    val data = input.readBytes()
                    val buffer = ByteBuffer.allocateDirect(data.size).apply {
                        order(ByteOrder.LITTLE_ENDIAN)
                        put(data)
                        position(PKM_HEADER_SIZE)
                    }
                    val header = ByteBuffer.allocateDirect(
                        PKM_HEADER_SIZE
                    ).apply {
                        order(ByteOrder.BIG_ENDIAN)
                        put(data, 0, PKM_HEADER_SIZE)
                        position(0)
                    }
                    val width = header.getShort(PKM_HEADER_WIDTH_OFFSET).toInt()
                    val height = header.getShort(PKM_HEADER_HEIGHT_OFFSET).toInt()
                    glCompressedTexImage2D(
                        GL_TEXTURE_2D, mipLevel, GL_COMPRESSED_RGBA8_ETC2_EAC, width, height,
                        0, data.size - PKM_HEADER_SIZE, buffer
                    )
                }
            }
            glBindTexture(GL_TEXTURE_2D, 0)
            return textures[0]
        }
    }
}