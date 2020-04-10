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

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import eu.tilk.wihajster.manifest.Attributes
import eu.tilk.wihajster.manifest.Manifest
import eu.tilk.wihajster.song.Song2014
import loggersoft.kotlin.streams.ByteOrder
import loggersoft.kotlin.streams.Stream
import loggersoft.kotlin.streams.StreamAdapter
import java.io.*
import java.nio.charset.Charset
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
        if (header.archiveFlags == 4u)
            decryptedStream(stream, header.totalTOCSize - 32, psarcKey)
        else stream
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

    fun listFiles(pattern : Regex) = manifest.keys.filter { pattern.matches(it) }

    fun inflateFile(name : String, outputStream : OutputStream) {
        val entryIdx = manifest[name]
        if (entryIdx == null) throw Exception("File not found")
        else inflateEntry(entries[entryIdx], outputStream)
    }

    fun inflateFile(name : String) : ByteArray {
        val data = ByteArrayOutputStream()
        inflateFile(name, data)
        return data.toByteArray()
    }

    fun inflateManifest(name : String) : Manifest {
        val data = inflateFile(name)
        val str = data.toString(Charset.forName("UTF8"))
        val mapper = JsonMapper().apply {
            registerModule(KotlinModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        return mapper.readValue(str)
    }

    fun inflateSng(name : String, attributes : Attributes) : Song2014 {
        val data = inflateFile(name)
        val stream = ByteArrayOutputStream(data.size - 24)
        decryptedSngStream(StreamAdapter(ByteArrayInputStream(data)), stream, data.size, sngKeyPC)
        val decStream = StreamAdapter(ByteArrayInputStream(stream.toByteArray()))
        val sngData = inflate(decStream)
        val sngStream = StreamAdapter(ByteArrayInputStream(sngData)).also {
            it.defaultByteOrder = ByteOrder.LittleEndian
        }
        return SongReader(sngStream, attributes).song
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
        private fun inflate(stream : Stream) : ByteArray {
            val size = stream.readInt()
            val outStream = ByteArrayOutputStream(size)
            val inflater = Inflater()
            val outBuffer = ByteArray(65536)
            while (!inflater.finished()) {
                if (inflater.needsInput()) {
                    val buffer = ByteArray(65536)
                    val bytes = stream.readBytes(buffer)
                    inflater.setInput(buffer, 0, bytes)
                }
                val bytes = inflater.inflate(outBuffer)
                if (bytes > 0)
                    outStream.write(outBuffer, 0, bytes)
            }
            return outStream.toByteArray()
        }

        private fun decryptedStream(stream : Stream, size : Int, key : ByteArray) : Stream {
            val cipher = Cipher.getInstance("AES/CFB/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE,
                SecretKeySpec(key, "AES"),
                IvParameterSpec(ByteArray(16)))
            fun ceilTo(x : Int, y : Int) = if (x % y == 0) x else x + y - x % y
            val store = ByteArray(ceilTo(size, 16))
            stream.readBytes(store, size)
            val result = cipher.doFinal(store)
            return StreamAdapter(ByteArrayInputStream(result)).also {
                it.defaultByteOrder = ByteOrder.BigEndian
            }
        }

        private fun decryptedSngStream(stream : Stream, outStream : OutputStream, size : Int, key : ByteArray) {
            val magic = stream.readInt()
            if (magic != 0x4a) throw Exception("Invalid SNG")
            stream.skip(4)
            val iv = ByteArray(16)
            stream.readBytes(iv)
            val cipher = Cipher.getInstance("AES/CFB/NoPadding")
            val buffer = ByteArray(16)
            for (i in 1 until size - 24 step 16) {
                cipher.init(Cipher.DECRYPT_MODE,
                    SecretKeySpec(key, "AES"),
                    IvParameterSpec(iv))
                stream.readBytes(buffer)
                outStream.write(cipher.doFinal(buffer))
                for (j in 15 downTo 0) {
                    iv[j]++
                    if (iv[j] != 0.toByte()) break
                }
            }
        }

        val psarcKey = ubyteArrayOf(
            0xC5U, 0x3DU, 0xB2U, 0x38U, 0x70U, 0xA1U, 0xA2U, 0xF7U,
            0x1CU, 0xAEU, 0x64U, 0x06U, 0x1FU, 0xDDU, 0x0EU, 0x11U,
            0x57U, 0x30U, 0x9DU, 0xC8U, 0x52U, 0x04U, 0xD4U, 0xC5U,
            0xBFU, 0xDFU, 0x25U, 0x09U, 0x0DU, 0xF2U, 0x57U, 0x2CU
        ).toByteArray()

        val sngKeyPC = ubyteArrayOf(
            0xCBU, 0x64U, 0x8DU, 0xF3U, 0xD1U, 0x2AU, 0x16U, 0xBFU,
            0x71U, 0x70U, 0x14U, 0x14U, 0xE6U, 0x96U, 0x19U, 0xECU,
            0x17U, 0x1CU, 0xCAU, 0x5DU, 0x2AU, 0x14U, 0x2EU, 0x3EU,
            0x59U, 0xDEU, 0x7AU, 0xDDU, 0xA1U, 0x8AU, 0x3AU, 0x30U
        ).toByteArray()
    }
}