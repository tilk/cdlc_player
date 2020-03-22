package eu.tilk.wihajster

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLES31.*
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.*
import eu.tilk.wihajster.song.Song2014
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.tan

const val COORDS_PER_VERTEX = 3
const val PKM_HEADER_SIZE = 16
const val PKM_HEADER_WIDTH_OFFSET = 8
const val PKM_HEADER_HEIGHT_OFFSET = 10

const val stringColorsGLSL = """
const vec3 stringColors[6] = vec3[](
    vec3(0.87, 0.33, 0.42),
    vec3(0.83, 0.76, 0.24),
    vec3(0.31, 0.69, 0.87),
    vec3(0.93, 0.69, 0.44),
    vec3(0.50, 0.85, 0.34),
    vec3(0.77, 0.29, 0.81)
);
"""

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

fun makeProgramFromShaders(vertexShader : Int, fragmentShader : Int) : Int {
    return glCreateProgram().also {
        glAttachShader(it, vertexShader)
        glAttachShader(it, fragmentShader)
        glLinkProgram(it)
        checkProgram(it)
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
    protected fun prepare(mvpMatrix : FloatArray) {
        glUseProgram(mProgram)
        val vPMatrixHandle = glGetUniformLocation(mProgram, "uMVPMatrix")
        glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)
    }
}

abstract class StaticShape(
    vertexCoords : FloatArray,
    drawOrder : ShortArray,
    mProgram : Int
) : Shape(vertexCoords, drawOrder, mProgram) {
    fun draw(mvpMatrix : FloatArray) {
        prepare(mvpMatrix)
        internalDraw()
    }
}

class Note : Shape(vertexCoords, drawOrder, mProgram) {
    companion object {
        private val vertexCoords = floatArrayOf(
            -0.25f, -0.12f, 0.0f,
            -0.25f, 0.12f, 0.0f,
            0.25f, 0.12f, 0.0f,
            0.25f, -0.12f, 0.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform vec4 uPosition;
            in vec4 vPosition;
            out vec2 vTexCoord;
            void main() {
                gl_Position = uMVPMatrix * (vPosition + uPosition);
                vTexCoord = vec2(vPosition.x / 0.25, vPosition.y / 0.12);
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            uniform int uString;
            in vec4 vTexCoord;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                float dist = max(abs(vTexCoord.x), abs(vTexCoord.y));
                float scaling = min(1.0, max(
                    1.0+(atan(1.0-20.0*abs(dist-0.8)))/3.14,
                    step(dist, 0.8) * (0.85 + vTexCoord.y / 4.0)
                ));
                FragColor = vec4(scaling * stringColors[uString], 1.0);
            }
        """.trimIndent()
        private var mProgram : Int = -1
        fun initialize() {
            val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
            mProgram = makeProgramFromShaders(vertexShader, fragmentShader)
        }
    }
    fun draw(mvpMatrix : FloatArray, time : Float, note : Event.Note, scrollSpeed : Float) {
        prepare(mvpMatrix)
        val positionHandle = glGetUniformLocation(mProgram, "uPosition")
        glUniform4f(positionHandle,
            note.fret - 0.5f,
            1.5f * (note.string + 0.5f) / 6f,
            (time - note.time) * scrollSpeed,
            0f)
        val stringHandle = glGetUniformLocation(mProgram, "uString")
        glUniform1i(stringHandle, note.string.toInt())
        internalDraw()
    }
}

class Neck : StaticShape(vertexCoords, drawOrder, mProgram) {
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
            in vec2 vTexCoord;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                float y = vTexCoord.y * 6.0;
                lowp int str = int(y);
                float dist = abs(y - float(str) - 0.5);
                float scl = (1.5+atan(20.0*(dist-0.1)))/3.0;
                FragColor = vec4(stringColors[str], 1.0 - scl);
            }
        """.trimIndent()
        private var mProgram : Int = -1
        fun initialize() {
            val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
            mProgram = makeProgramFromShaders(vertexShader, fragmentShader)
        }
    }
}

class FretNumbers(private val textures : Textures) : StaticShape(vertexCoords, drawOrder, mProgram) {
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
                vTexCoord = vec2(vPosition.x, -vPosition.y / 0.5);
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            varying vec2 vTexCoord;
            void main() {
                float x = fract(vTexCoord.x);
                float y = vTexCoord.y;
                lowp int fret = int(vTexCoord.x);
                lowp int col = fret/12;
                gl_FragColor = texture2D(uTexture, vec2((x + float(col)) / 6.0, (y + float(fret - 12 * col)) / 12.0));
            }
        """.trimIndent()
        private var mProgram : Int = -1
        fun initialize() {
            val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
            mProgram = makeProgramFromShaders(vertexShader, fragmentShader)
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
            -0.5f, 0.0f, 0.0f,
            -0.5f, 0.0f, -1.0f,
            24.5f, 0.0f, -1.0f,
            24.5f, 0.0f, 0.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            in vec4 vPosition;
            uniform vec2 uTime;
            out float pos;
            void main() {
                vec4 actPosition = vec4(vPosition.x, vPosition.y, uTime.x + vPosition.z * uTime.y, vPosition.w);
                gl_Position = uMVPMatrix * actPosition;
                pos = vPosition.x;
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            uniform ivec2 uFret;
            in float pos;
            out vec4 FragColor;
            const vec3 beltColor = vec3(0.063, 0.231, 0.365);
            const vec3 bumpColor = vec3(0.051, 0.388, 0.478);
            void main() {
                float fdist = 2.0 * distance(fract(pos), 0.5);
                float cdist = 1.0 - fdist;
                FragColor = vec4(
                    step(float(uFret.x - 1), pos) * step(pos, float(uFret.x + uFret.y - 1)) * (cos(2.0*fdist)+1.0)/2.0 * beltColor
                    + (tanh(20.0*(fdist-0.95))+1.0)/2.0 * bumpColor, 1.0);
            }
        """.trimIndent()
        private var mProgram : Int = -1
        fun initialize() {
            val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
            mProgram = makeProgramFromShaders(vertexShader, fragmentShader)
        }
    }
    fun draw(
        mvpMatrix : FloatArray,
        time : Float,
        anchor : Event.Anchor,
        lastAnchor : Event.Anchor,
        scrollSpeed : Float
    ) {
        prepare(mvpMatrix)
        val timeHandle = glGetUniformLocation(mProgram, "uTime")
        glUniform2f(timeHandle,
            (time - anchor.time) * scrollSpeed,
            (lastAnchor.time - anchor.time) * scrollSpeed)
        val fretHandle = GLES20.glGetUniformLocation(mProgram, "uFret")
        glUniform2i(fretHandle, anchor.fret.toInt(), anchor.width.toInt())
        internalDraw()
    }
}

class SongScroller(private val song : List<Event>, val horizon : Float) {
    private var time : Float = 0F
    private var position : Int = 0
    private var events : ArrayList<Event> = ArrayList()

    val activeEvents : List<Event> get() = events
    val currentTime : Float get() = time

    fun advance(t : Float) {
        time += t

        val it = events.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e.endTime < time) it.remove()
        }

        while (position < song.size && song[position].time < time + horizon)
            events.add(song[position++])
    }
}

class MyGLRenderer(val song : List<Event>, private val context : Context) : GLSurfaceView.Renderer {
    private lateinit var neck : Neck
    private lateinit var tab : Tab
    private lateinit var textures : Textures
    private lateinit var fretNumbers : FretNumbers
    private lateinit var note : Note
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private var lastFrameTime : Long = 0
    private var scrollSpeed : Float = 13f
    private val scroller : SongScroller = SongScroller(song, 100f / scrollSpeed)
    private var awayAnchor : Event.Anchor = Event.Anchor(0f, -1 ,0)
    private var finalAnchor : Event.Anchor = awayAnchor

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //glEnable(GL_DEPTH_TEST)
        Neck.initialize()
        FretNumbers.initialize()
        Tab.initialize()
        Note.initialize()
        textures = Textures(context)
        neck = Neck()
        fretNumbers = FretNumbers(textures)
        tab = Tab()
        note = Note()
        lastFrameTime = SystemClock.elapsedRealtime()
    }

    override fun onDrawFrame(gl: GL10?) {
        val currentTime = SystemClock.elapsedRealtime()
        val deltaTime = currentTime - lastFrameTime
        lastFrameTime = currentTime

        scroller.advance(deltaTime / 1000.0F)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        Matrix.setLookAtM(viewMatrix, 0, 4f, 3f, 4f, 4f, 1.5f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        var lastAnchor = Event.Anchor(currentTime + scroller.horizon, -1, 0)
        for (evt in scroller.activeEvents.reversed()) {
            when (evt) {
                is Event.Anchor -> {
                    tab.draw(vPMatrix, scroller.currentTime, evt, lastAnchor, scrollSpeed)
                    lastAnchor = evt
                }
            }
        }
        if (finalAnchor.time < scroller.currentTime)
            awayAnchor = Event.Anchor(scroller.currentTime, finalAnchor.fret, finalAnchor.width)
        else
            awayAnchor = Event.Anchor(scroller.currentTime, awayAnchor.fret, awayAnchor.width)
        if (lastAnchor.width > 0) finalAnchor = lastAnchor
        tab.draw(vPMatrix, scroller.currentTime, awayAnchor, lastAnchor, scrollSpeed)

        for (evt in scroller.activeEvents.reversed()) {
            when (evt) {
                is Event.Note -> note.draw(vPMatrix, scroller.currentTime, evt, scrollSpeed)
            }
        }

        neck.draw(vPMatrix)
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
        val song = loadSong("songs/sabaprim_lead.xml")
        renderer = MyGLRenderer(song.makeEventList(), context)
        setRenderer(renderer)
    }
    private fun loadSong(name : String) : Song2014 {
        val xmlMapper = XmlMapper().apply {
            registerModule(KotlinModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        return context.resources.assets.open(name).use { input ->
            xmlMapper.readValue(input)
        }
    }
}
