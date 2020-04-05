package eu.tilk.wihajster.psarc

import loggersoft.kotlin.streams.StreamInput
import java.lang.Exception

data class Header @ExperimentalUnsignedTypes constructor(
    val magicNumber : UInt = 1347633490u, // PSAR
    val versionNumber : UInt = 65540u, // 1.4
    val compressionMethod: UInt = 2053925218u, // zlib
    val totalTOCSize : Int,
    val TOCEntrySize : Int = 30,
    val numFiles : Int,
    val blockSizeAlloc : Int = 65536,
    val archiveFlags : UInt = 0u
) {
    @ExperimentalUnsignedTypes
    companion object {
        fun read(stream : StreamInput) : Header {
            val magicNumber = stream.readInt().toUInt()
            if (magicNumber != 1347633490u)
                throw Exception("Invalid PSARC magic number: $magicNumber")
            val versionNumber = stream.readInt().toUInt()
            val compressionMethod = stream.readInt().toUInt()
            val totalTOCSize = stream.readInt()
            val tocEntrySize = stream.readInt()
            val numFiles = stream.readInt()
            val blockSizeAlloc = stream.readInt()
            val archiveFlags = stream.readInt().toUInt()
            return Header(magicNumber, versionNumber, compressionMethod, totalTOCSize,
                tocEntrySize, numFiles, blockSizeAlloc, archiveFlags)
        }
    }
}