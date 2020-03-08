package eu.tilk.wihajster

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLES31.*
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.tan

const val COORDS_PER_VERTEX = 3
const val PKM_HEADER_SIZE = 16
const val PKM_HEADER_WIDTH_OFFSET = 8
const val PKM_HEADER_HEIGHT_OFFSET = 10

fun loadShader(type : Int, shaderCode : String): Int {
    return glCreateShader(type).also {
        glShaderSource(it, shaderCode)
        glCompileShader(it)
    }
}

fun checkProgram(n : Int) {
    val buf = IntBuffer.allocate(1)
    glGetProgramiv(n, GL_LINK_STATUS, buf)
    if (buf[0] != GL_TRUE) {
        Log.e("program", glGetProgramInfoLog(n))
    }
}

fun loadTexture(context : Context, fileName : String, lastMip : Int) : Int {
    val textures = IntArray(1)
    glGenTextures(1, textures, 0)
    glBindTexture(GL_TEXTURE_2D, textures[0])
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    for (mipLevel in 0..lastMip) {
        val name = fileName + "_mip_" + mipLevel + ".pkm"
        context.resources.assets.open(name).use { input ->
            val data = input.readBytes()
            val buffer = ByteBuffer.allocateDirect(data.size).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put(data)
                position(PKM_HEADER_SIZE)
            }
            val header = ByteBuffer.allocateDirect(PKM_HEADER_SIZE).apply {
                order(ByteOrder.BIG_ENDIAN)
                put(data, 0, PKM_HEADER_SIZE)
                position(0)
            }
            val width = header.getShort(PKM_HEADER_WIDTH_OFFSET).toInt()
            val height = header.getShort(PKM_HEADER_HEIGHT_OFFSET).toInt()
            glCompressedTexImage2D(
                GL_TEXTURE_2D, mipLevel, GL_COMPRESSED_RGBA8_ETC2_EAC, width, height,
                0, data.size - PKM_HEADER_SIZE, buffer
            )
        }
    }
    glBindTexture(GL_TEXTURE_2D, 0)
    return textures[0]
}

class Textures(context : Context) {
    val fretNumbers = loadTexture(context, "textures/fretNumbers", 9)
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

class Neck(textures : Textures) : Shape(vertexCoords, drawOrder, mProgram) {
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
        fun initialize() {
            mProgram = glCreateProgram().also {
                val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
                val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
                glAttachShader(it, vertexShader)
                glAttachShader(it, fragmentShader)
                glLinkProgram(it)
                checkProgram(it)
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

class FretNumbers(private val textures : Textures) : Shape(vertexCoords, drawOrder, mProgram) {
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
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vTexCoord = vec2(vPosition.x, vPosition.y);
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            varying vec2 vTexCoord;
            void main() {
                float x = fract(vTexCoord.x);
                float y = -vTexCoord.y / 0.5;
                lowp int fret = int(vTexCoord.x);
                lowp int col = fret/12;
                gl_FragColor = texture2D(uTexture, vec2((x + float(col)) / 6.0, (y + float(fret - 12 * col)) / 12.0));
            }
        """.trimIndent()
        private var mProgram : Int = -1
        fun initialize() {
            mProgram = glCreateProgram().also {
                val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
                val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
                glAttachShader(it, vertexShader)
                glAttachShader(it, fragmentShader)
                glLinkProgram(it)
                checkProgram(it)
            }
        }
    }
    override fun internalDraw() {
        glGetUniformLocation(mProgram, "uTexture").also {
            glUniform1i(it, 0)
        }
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textures.fretNumbers)
        super.internalDraw()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}

class Tab : Shape(vertexCoords, drawOrder, mProgram) {
    companion object {
        private val vertexCoords = floatArrayOf(
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, -100.0f,
            24.0f, 0.0f, -100.0f,
            24.0f, 0.0f, 0.0f
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
        fun initialize() {
            mProgram = glCreateProgram().also {
                val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
                val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
                glAttachShader(it, vertexShader)
                glAttachShader(it, fragmentShader)
                glLinkProgram(it)
                checkProgram(it)
            }
        }
    }
    override fun internalDraw() {
        glGetUniformLocation(mProgram, "vColor").also {
            val color = floatArrayOf(0.22265625f, 0.63671875f, 0.76953125f, 1.0f)
            glUniform4fv(it, 1, color, 0)
        }
        super.internalDraw()
    }
}

class MyGLRenderer(private val context : Context) : GLSurfaceView.Renderer {
    private lateinit var neck : Neck
    private lateinit var tab : Tab
    private lateinit var textures : Textures
    private lateinit var fretNumbers : FretNumbers
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glEnable(GL_DEPTH_TEST)
        Neck.initialize()
        FretNumbers.initialize()
        Tab.initialize()
        textures = Textures(context)
        neck = Neck(textures)
        fretNumbers = FretNumbers(textures)
        tab = Tab()
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        Matrix.setLookAtM(viewMatrix, 0, 12f, 3f, 4f, 12f, 1.5f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        neck.draw(vPMatrix)
        tab.draw(vPMatrix)
        fretNumbers.draw(vPMatrix)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        val fov = 0.9f
        val zNear = 0.1f
        val zFar = 100.0f
        val size = zNear * tan(fov / 2)
        Matrix.frustumM(projectionMatrix, 0,
            -size*ratio, size*ratio, -size, size, zNear, zFar)
    }
}

class MyGLSurfaceView(context : Context) : GLSurfaceView(context) {
    private val renderer : MyGLRenderer
    init {
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 16, 4)
        renderer = MyGLRenderer(context)
        setRenderer(renderer)
    }
}
