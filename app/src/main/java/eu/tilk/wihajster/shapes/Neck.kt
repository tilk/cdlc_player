package eu.tilk.wihajster.shapes

import android.opengl.GLES31.*

class Neck(private val activeStrings : Int) : StaticShape(vertexCoords, drawOrder, mProgram) {
    companion object {
        private val vertexCoords = floatArrayOf(
            0.0f, 0f, 0.0f,
            0.0f, 1.5f, 0.0f,
            24.0f, 1.5f, 0.0f,
            24.0f, 0f, 0.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            in vec4 vPosition;
            out vec2 vTexCoord;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vTexCoord = vec2(vPosition.x, vPosition.y / 1.5);
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            uniform int uStrings;
            in vec2 vTexCoord;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                float y = vTexCoord.y * 6.0;
                lowp int str = int(y);
                float dist = abs(y - float(str) - 0.5);
                float scl = (1.5+atan(20.0*(dist-0.1)))/3.0;
                float coef = (1.0 + float((uStrings & (1 << str)) != 0)) / 2.0;
                FragColor = vec4(coef * stringColors[str], 1.0 - scl);
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
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        glGetUniformLocation(mProgram, "uStrings").also {
            glUniform1i(it, activeStrings)
        }
        super.internalDraw(time, scrollSpeed)
    }
}