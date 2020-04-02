package eu.tilk.wihajster.shapes

import eu.tilk.wihajster.Textures
import android.opengl.GLES31.*

class FretNumbers(
    private val textures : Textures,
    private val leftFret : Int,
    private val rightFret : Int
) : StaticShape(vertexCoords, drawOrder, mProgram) {
    companion object {
        private val vertexCoords = floatArrayOf(
            0.0f, -0.5f, 0.0f,
            0.0f, 0f, 0.0f,
            24.0f, 0f, 0.0f,
            24.0f, -0.5f, 0.0f
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
                vTexCoord = vec2(vPosition.x, -vPosition.y / 0.5);
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            uniform sampler2D uTexture;
            uniform ivec2 uFrets;
            in vec2 vTexCoord;
            out vec4 FragColor;
            void main() {
                float x = fract(vTexCoord.x);
                float y = vTexCoord.y;
                lowp int fret = int(vTexCoord.x);
                lowp int col = fret/12;
                if (fret+1 < uFrets.x || fret+1 > uFrets.y-1) col += 2;
                FragColor = texture(uTexture, vec2((x + float(col)) / 6.0, (y + float(fret - 12 * col)) / 12.0));
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
        glGetUniformLocation(mProgram, "uFrets").also {
            glUniform2i(it, leftFret, rightFret)
        }
        glGetUniformLocation(mProgram, "uTexture").also {
            glUniform1i(it, 0)
        }
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textures.fretNumbers)
        super.internalDraw(time, scrollSpeed)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}