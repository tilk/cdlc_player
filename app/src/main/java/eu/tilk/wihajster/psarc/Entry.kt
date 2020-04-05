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