package eu.tilk.wihajster.shapes

import eu.tilk.wihajster.Event
import eu.tilk.wihajster.SortLevel
import android.opengl.GLES31.*
import kotlin.math.ceil

class NoteTail(note : Event.Note) : EventShape<Event.Note>(makeVertexCoords(note), makeDrawOrder(note), mProgram, note) {
    companion object {
        private const val scaling = 10
        private fun sizeFor(note : Event.Note) : Int = ceil(note.sustain * scaling).toInt()
        private fun makeVertexCoords(note : Event.Note) = FloatArray(6 + 6 * sizeFor(note)) {
            val i = it / 3
            when (it % 3) {
                0 -> ((i % 2).toFloat() - 0.5f) / 2f
                1 -> 0f
                2 -> -i.toFloat() / scaling
                else -> error("Should not happen!")
            }
        }
        private val drawOrder = shortArrayOf(0, 1, 2, 1, 3, 2)
        private fun makeDrawOrder(note : Event.Note) = ShortArray(6 * sizeFor(note)) {
            (drawOrder[it % 6] + 2 * (it / 6)).toShort()
        }
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform vec4 uPosition;
            in vec4 vPosition;
            out float zPos;
            void main() {
                vec4 pos = vPosition + uPosition;
                gl_Position = uMVPMatrix * pos;
                zPos = pos.z;
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            uniform int uString;
            in float zPos;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                FragColor = vec4(stringColors[uString], step(zPos, 0.0));
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
    override val endTime = note.time + note.sustain
    override val sortLevel =
        SortLevel.StringTail(note.string.toInt())
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        val positionHandle = glGetUniformLocation(mProgram, "uPosition")
        glUniform4f(positionHandle,
            event.fret - 0.5f,
            1.5f * (event.string + 0.5f) / 6f,
            (time - event.time) * scrollSpeed,
            0f)
        val stringHandle = glGetUniformLocation(mProgram, "uString")
        glUniform1i(stringHandle, event.string.toInt())
        super.internalDraw(time, scrollSpeed)
    }
}