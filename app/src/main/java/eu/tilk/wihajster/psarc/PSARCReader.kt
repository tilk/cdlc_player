package eu.tilk.wihajster.psarc

import loggersoft.kotlin.streams.ByteOrder
import loggersoft.kotlin.streams.StreamAdapter
import java.io.*
import java.util.zip.Inflater
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.log

@ExperimentalUnsignedTypes
class PSARCReader(private val inputStream : FileInputStream) {
    private val stream = StreamAdapter(inputStream).also {
        it.defaultByteOrder = ByteOrder.BigEndian
    }
    private val initPosition = inputStream.channel.position()
    private val header : Header = Header.read(stream)
    private val tocStream = {
        if (header.archiveFlags == 4u) {
            val cipher = Cipher.getInstance("AES/CFB/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE,
                SecretKeySpec(psarcKey, "AES"),
                IvParameterSpec(ByteArray(16)))
            fun ceilTo(x : Int, y : Int) = if (x % y == 0) x else x + y - x % y
            val store = ByteArray(ceilTo(header.totalTOCSize - 32, 128))
            stream.readBytes(store, header.totalTOCSize - 32)
            val result = cipher.doFinal(store)
            StreamAdapter(ByteArrayInputStream(result)).also {
                it.defaultByteOrder = ByteOrder.BigEndian
            }
        } else stream
    }()
    private val entries : ArrayList<Entry> = ArrayList()
    init {
        for (i in 0 until header.numFiles)
            entries.add(Entry.read(tocStream))
    }
    private val bNum = log(header.blockSizeAlloc.toDouble(), 256.0).toInt()
    private val tocSize = header.totalTOCSize - 32
    private val tocChunkSize = header.numFiles * header.TOCEntrySize
    private val zBlocksSizeList : IntArray = IntArray((tocSize - tocChunkSize) / bNum)
    init {
        for (i in zBlocksSizeList.indices)
            zBlocksSizeList[i] = tocStream.readInt(bNum, false).toInt()
    }
    private val manifest = HashMap<String, Int>()
    init {
        val data = ByteArrayOutputStream()
        inflateEntry(entries[0], data)
        val names = data.toString().split('\n')
        for (i in names.indices) manifest[names[i]] = i + 1
    }

    fun inflateFile(name : String, outputStream : OutputStream) {
        val entryIdx = manifest[name]
        if (entryIdx == null) throw Exception("File not found")
        else inflateEntry(entries[entryIdx], outputStream)
    }

    private fun inflateEntry(entry : Entry, outputStream : OutputStream) {
        var length = 0
        fun output(data : ByteArray, count : Int = data.size) {
            outputStream.write(data, 0, count)
            length += count
        }
        var zChunkID = entry.zIndexBegin
        inputStream.channel.position(initPosition + entry.offset)
        while (length < entry.length) {
            if (zBlocksSizeList[zChunkID] == 0) {
                val buffer = ByteArray(header.blockSizeAlloc)
                stream.readBytes(buffer, header.blockSizeAlloc)
                output(buffer)
            } else {
                val num = stream.readInt(2, false).toInt()
                inputStream.channel.position(inputStream.channel.position() - 2)
                val buffer = ByteArray(zBlocksSizeList[zChunkID])
                stream.readBytes(buffer)
                if (num == 0x78da) { // wtf, hacky
                    val inflater = Inflater()
                    inflater.setInput(buffer)
                    val obuf = ByteArray(65536)
                    while (true) {
                        val bytes = inflater.inflate(obuf)
                        if (bytes == 0) break
                        output(obuf, bytes)
                    }
                } else output(buffer)
            }
            zChunkID++
        }
    }

    companion object {
        private val psarcKey = ubyteArrayOf(
            0xC5U, 0x3DU, 0xB2U, 0x38U, 0x70U, 0xA1U, 0xA2U, 0xF7U,
            0x1CU, 0xAEU, 0x64U, 0x06U, 0x1FU, 0xDDU, 0x0EU, 0x11U,
            0x57U, 0x30U, 0x9DU, 0xC8U, 0x52U, 0x04U, 0xD4U, 0xC5U,
            0xBFU, 0xDFU, 0x25U, 0x09U, 0x0DU, 0xF2U, 0x57U, 0x2CU
        ).toByteArray()
    }
}