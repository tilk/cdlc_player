package eu.tilk.wihajster.shapes

import eu.tilk.wihajster.Event
import eu.tilk.wihajster.SortLevel
import android.opengl.GLES31.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.ceil
import kotlin.math.tanh

// TODO vibrato, bendy, zanikanie przy unpitched slide
class NoteTail(note : Event.Note, anchor : Event.Anchor, scrollSpeed : Float) :
    EventShape<Event.Note>(
        makeVertexCoords(note, scrollSpeed),
        makeDrawOrder(note, scrollSpeed),
        mProgram,
        note
    ) {
    companion object {
        private const val scaling = 10
        private fun sizeFor(note : Event.Note, scrollSpeed : Float) : Int =
            ceil(note.sustain * scrollSpeed * scaling).toInt()
        private fun makeVertexCoords(note : Event.Note, scrollSpeed : Float) : FloatArray {
            val slideLen = when {
                note.slideTo > 0 -> note.slideTo - note.fret
                note.slideUnpitchedTo >= 0 -> note.slideUnpitchedTo - note.fret
                else -> 0
            }
            val sz = sizeFor(note, scrollSpeed)
            fun logistic(x : Float) = 0.5f + 0.5f * tanh(x)
            fun dLogistic(x : Float) = logistic(x) * logistic(-x)
            return FloatArray(6 + 6 * sz) {
                val i = it / 3 // vertex number
                val z = i / 2  // Z axis distance
                val pct = z.toFloat() / sz
                when (it % 3) {
                    0 -> {
                        var v = ((i % 2).toFloat() - 0.5f) / 4f
                        // add slide effect
                        if (slideLen != 0) v = v * (1f + 10f * dLogistic(pct * 10f - 5f) / note.sustain / scrollSpeed * slideLen) +
                            slideLen * logistic(pct * 10f - 5f)
                        // add tremolo effect
                        if (note.tremolo > 0)
                            when (z % 8) {
                                1,3 -> v += 0.05f
                                2 -> v += 0.1f
                                5,7 -> v -= 0.05f
                                6 -> v -= 0.1f
                            }
                        v
                    }
                    1 -> 0f
                    2 -> -z.toFloat() / scaling
                    else -> error("Should not happen!")
                }
            }
        }
        private val drawOrder = shortArrayOf(0, 1, 2, 1, 3, 2)
        private fun makeDrawOrder(note : Event.Note, scrollSpeed : Float) : ShortArray {
            return ShortArray(6 * sizeFor(note, scrollSpeed)) {
                (drawOrder[it % 6] + 2 * (it / 6)).toShort()
            }
        }
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform vec4 uPosition;
            uniform float uMaxZ;
            in vec4 vPosition;
            in float vParity;
            out float zPos;
            out vec2 vTexCoord;
            void main() {
                vec4 pos = vPosition + uPosition;
                gl_Position = uMVPMatrix * pos;
                zPos = pos.z;
                vTexCoord = vec2(vParity, vPosition.z / uMaxZ);
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            uniform int uString;
            in float zPos;
            in vec2 vTexCoord;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                float dist = abs(vTexCoord.x);
                float scaling = min(1.0, 1.0+(atan(1.0-20.0*abs(dist-0.8)))/3.14);
                FragColor = vec4(scaling * stringColors[uString], 
                    step(zPos, 0.0) * clamp(40.0 + zPos, 0.0, 1.0));
            }
        """.trimIndent()
        private var mProgram : Int = -1
        fun initialize() {
            val vertexShader =
                loadShader(
                    GL_VERTEX_SHADER,
                    vertexShaderCode
                )
            val fragmentShader = loadShader(
                GL_FRAGMENT_SHADER,
                fragmentShaderCode
            )
            mProgram = makeProgramFromShaders(
                vertexShader,
                fragmentShader
            )
        }
    }

    private val parityBuffer : FloatBuffer =
        ByteBuffer.allocateDirect(8 + sizeFor(note, scrollSpeed) * 8).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                for (i in 0..sizeFor(note, scrollSpeed)) {
                    put(-1f)
                    put(1f)
                }
                position(0)
            }
        }
    override val endTime = note.time + note.sustain
    override val sortLevel =
        SortLevel.StringTail(note.string.toInt())
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        val parityHandle = glGetAttribLocation(mProgram, "vParity")
        glEnableVertexAttribArray(parityHandle)
        glVertexAttribPointer(parityHandle,
            1, GL_FLOAT, false,
            4, parityBuffer)
        val positionHandle = glGetUniformLocation(mProgram, "uPosition")
        glUniform4f(positionHandle,
            event.fret - 0.5f,
            1.5f * (event.string + 0.5f) / 6f,
            (time - event.time) * scrollSpeed,
            0f)
        val stringHandle = glGetUniformLocation(mProgram, "uString")
        glUniform1i(stringHandle, event.string.toInt())
        val maxZHandle = glGetUniformLocation(mProgram, "uMaxZ")
        glUniform1f(maxZHandle, event.sustain * scrollSpeed)
        super.internalDraw(time, scrollSpeed)
        glDisableVertexAttribArray(parityHandle)
    }
}