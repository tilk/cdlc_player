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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.opengl.GLES31.*
import android.opengl.GLUtils
import eu.tilk.wihajster.song.ChordTemplate2014
import eu.tilk.wihajster.song.Song2014

class Textures(context : Context, song : Song2014) {
    val fretNumbers =
        loadTexture(context, "textures/fretNumbers", 9)
    val effects =
        loadTexture(context, "textures/effects", 9)
    val chordTextures =
        makeChordTextures(song.chordTemplates)
    companion object {
        private const val PKM_HEADER_SIZE = 16
        private const val PKM_HEADER_WIDTH_OFFSET = 8
        private const val PKM_HEADER_HEIGHT_OFFSET = 10
        fun makeChordTextures(
            chordTemplates : List<ChordTemplate2014>
        ) : Int {
            val bitmap = Bitmap.createBitmap(256, 64 * maxOf(1, chordTemplates.size), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            bitmap.eraseColor(0)
            val textPaint = Paint()
            textPaint.textSize = 48f
            textPaint.isAntiAlias = true
            textPaint.setARGB(0xff, 0xff, 0xff, 0xff)
            for (i in chordTemplates.indices)
                canvas.drawText(chordTemplates[i].chordName, 0f, (i+1) * 64f - 10f, textPaint)

            val textures = IntArray(1)
            glGenTextures(1, textures, 0)
            glBindTexture(GL_TEXTURE_2D, textures[0])
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
            return textures[0]
        }
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