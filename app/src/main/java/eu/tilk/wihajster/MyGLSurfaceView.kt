package eu.tilk.wihajster

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.GLES31.*
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.tan

val COORDS_PER_VERTEX = 3

fun loadShader(type : Int, shaderCode : String): Int {
    return glCreateShader(type).also {
        glShaderSource(it, shaderCode)
        glCompileShader(it)
    }
}

abstract class Shape(
    private val vertexCoords : FloatArray,
    private val drawOrder : ShortArray,
    private val mProgram : Int
) {
    private val vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(vertexCoords)
            position(0)
        }
    }
    private val drawListBuffer = ByteBuffer.allocateDirect(drawOrder.size * 2).run {
        order(ByteOrder.nativeOrder())
        asShortBuffer().apply {
            put(drawOrder)
            position(0)
        }
    }
    protected open fun internalDraw() {
        val positionHandle = glGetAttribLocation(mProgram, "vPosition")
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GL_FLOAT, false,
            COORDS_PER_VERTEX * 4, vertexBuffer)
        glDrawElements(GL_TRIANGLES, drawOrder.size, GL_UNSIGNED_SHORT, drawListBuffer)
        glDisableVertexAttribArray(positionHandle)
    }
    fun draw(mvpMatrix : FloatArray) {
        glUseProgram(mProgram)
        val vPMatrixHandle = glGetUniformLocation(mProgram, "uMVPMatrix")
        glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)
        internalDraw()
    }
}

class Neck : Shape(vertexCoords, drawOrder, mProgram) {
    companion object {
        private val vertexCoords = floatArrayOf(
            0.0f, -1.5f, 0.0f,
            0.0f, 0.0f, 0.0f,
            24.0f, 0.0f, 0.0f,
            24.0f, -1.5f, 0.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            precision mediump float;
            uniform vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()
        private var mProgram : Int = -1
        fun makeProgram() {
            mProgram = glCreateProgram().also {
                val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
                val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
                glAttachShader(it, vertexShader)
                glAttachShader(it, fragmentShader)
                glLinkProgram(it)
            }
        }
    }
    override fun internalDraw() {
        glGetUniformLocation(mProgram, "vColor").also {
            val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
            glUniform4fv(it, 1, color, 0)
        }
        super.internalDraw()
    }
}

class MyGLRenderer : GLSurfaceView.Renderer {
    private lateinit var neck : Neck
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        Neck.makeProgram()
        neck = Neck()
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        neck.draw(vPMatrix)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        val fov = 0.75f
        val zNear = 0.1f
        val zFar = 100.0f
        val size = zNear * tan(fov / 2)
        Matrix.frustumM(projectionMatrix, 0,
            -size, size, -size/ratio, size/ratio, zNear, zFar)
    }
}

class MyGLSurfaceView(context : Context) : GLSurfaceView(context) {
    private val renderer : MyGLRenderer
    init {
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 16, 4)
        renderer = MyGLRenderer()
        setRenderer(renderer)
    }
}
