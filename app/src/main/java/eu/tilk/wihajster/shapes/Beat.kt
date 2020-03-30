package eu.tilk.wihajster.shapes

import eu.tilk.wihajster.Event
import eu.tilk.wihajster.SortLevel
import android.opengl.GLES31.*

class Beat(
    beat : Event.Beat,
    private val anchor : Event.Anchor
) : EventShape<Event.Beat>(vertexCoords, drawOrder, mProgram, beat) {
    companion object {
        private val vertexCoords = floatArrayOf(
            0f, 0f, 0.0f,
            0f, 0f, 0.15f,
            1f, 0f, 0.15f,
            1f, 0f, 0.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform float uTime;
            uniform ivec2 uFret;
            in vec4 vPosition;
            out vec2 vTexCoord;
            void main() {
                vec4 actPosition = vec4(float(uFret.x - 1) + vPosition.x * float(uFret.y), vPosition.y, uTime + vPosition.z, vPosition.w);
                gl_Position = uMVPMatrix * actPosition;
                vTexCoord = vec2(vPosition.x, vPosition.y);
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            in vec2 vTexCoord;
            out vec4 FragColor;
            uniform int uMeasure;
            $bumpColorGLSL
            void main() {
                FragColor = vec4(bumpColor, 0.5 + float(uMeasure) / 2.0);
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
    override val sortLevel = SortLevel.Beat
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        val timeHandle = glGetUniformLocation(mProgram, "uTime")
        glUniform1f(timeHandle, (time - event.time) * scrollSpeed)
        val fretHandle = glGetUniformLocation(mProgram, "uFret")
        glUniform2i(fretHandle, anchor.fret.toInt(), anchor.width.toInt())
        val measureHandle = glGetUniformLocation(mProgram, "uMeasure")
        glUniform1i(measureHandle, if (event.measure >= 0) 1 else 0)
        super.internalDraw(time, scrollSpeed)
    }
}