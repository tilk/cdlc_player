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

package eu.tilk.wihajster.psarc

import loggersoft.kotlin.streams.StreamInput

data class Entry @ExperimentalUnsignedTypes constructor(
    val md5 : ByteArray,
    val zIndexBegin : Int,
    val length : Long,
    val offset : Long
) {
    @ExperimentalUnsignedTypes
    companion object {
        fun read(stream : StreamInput) : Entry {
            val md5 = ByteArray(16)
            stream.readBytes(md5, 16)
            val zIndexBegin = stream.readInt()
            val length = stream.readInt(5)
            val offset = stream.readInt(5)
            return Entry(md5, zIndexBegin, length, offset)
        }
    }
}