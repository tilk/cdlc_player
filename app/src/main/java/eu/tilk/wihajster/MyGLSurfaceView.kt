package eu.tilk.wihajster

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
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
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.collections.ArrayList
import kotlin.math.max
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
const val beltColorGLSL = "const vec3 beltColor = vec3(0.063, 0.231, 0.365);"
const val bumpColorGLSL = "const vec3 bumpColor = vec3(0.051, 0.388, 0.478);"

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
    protected open fun internalDraw(time: Float, scrollSpeed : Float) {
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
    fun draw(mvpMatrix : FloatArray, time: Float, scrollSpeed : Float) {
        prepare(mvpMatrix)
        internalDraw(time, scrollSpeed)
    }
}

abstract class EventShape<out T : Event>(
    vertexCoords : FloatArray,
    drawOrder : ShortArray,
    mProgram : Int,
    val event : T
) : StaticShape(vertexCoords, drawOrder, mProgram) {
    open val endTime : Float get() = event.time
    abstract val sortLevel : SortLevel
}

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
            val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
            mProgram = makeProgramFromShaders(vertexShader, fragmentShader)
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

class Note(note : Event.Note) : EventShape<Event.Note>(vertexCoords, drawOrder, mProgram, note) {
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
            in vec2 vTexCoord;
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
    override val sortLevel = SortLevel.String(note.string.toInt())
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

class EmptyStringNote(
    note : Event.Note,
    private val anchor : Event.Anchor
) : EventShape<Event.Note>(vertexCoords, drawOrder, mProgram, note) {
    companion object {
        private val vertexCoords = floatArrayOf(
            0f, -0.12f, 0.0f,
            0f, 0.12f, 0.0f,
            1f, 0.12f, 0.0f,
            1f, -0.12f, 0.0f
        )
        private val drawOrder = shortArrayOf(
            0, 1, 2, 0, 2, 3
        )
        private val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform vec4 uPosition;
            uniform int uWidth;
            in vec4 vPosition;
            out vec2 vTexCoord;
            void main() {
                vec4 rPosition = vec4(vPosition.x * float(uWidth), vPosition.y, vPosition.z, vPosition.w); 
                gl_Position = uMVPMatrix * (rPosition + uPosition);
                vTexCoord = vec2((vPosition.x - 0.5) / 0.5, vPosition.y / 0.12);
            }
        """.trimIndent()
        private val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            uniform int uString;
            in vec2 vTexCoord;
            out vec4 FragColor;
            $stringColorsGLSL
            void main() {
                float scl = (tanh(-abs(vTexCoord.y*10.0)+3.0) + 1.0) / 2.0;
                FragColor = vec4(stringColors[uString], scl);
            }
        """.trimIndent()
        private var mProgram : Int = -1
        fun initialize() {
            val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
            mProgram = makeProgramFromShaders(vertexShader, fragmentShader)
        }
    }
    override val sortLevel = SortLevel.String(note.string.toInt())
    override fun internalDraw(time: Float, scrollSpeed : Float) {
        val positionHandle = glGetUniformLocation(mProgram, "uPosition")
        glUniform4f(positionHandle,
            anchor.fret - 1f,
            1.5f * (event.string + 0.5f) / 6f,
            (time - event.time) * scrollSpeed,
            0f)
        val fretHandle = glGetUniformLocation(mProgram, "uWidth")
        glUniform1i(fretHandle, anchor.width.toInt())
        val stringHandle = glGetUniformLocation(mProgram, "uString")
        glUniform1i(stringHandle, event.string.toInt())
        super.internalDraw(time, scrollSpeed)
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
            in vec2 vTexCoord;
            out vec4 FragColor;
            void main() {
                float x = fract(vTexCoord.x);
                float y = vTexCoord.y;
                lowp int fret = int(vTexCoord.x);
                lowp int col = fret/12;
                FragColor = texture(uTexture, vec2((x + float(col)) / 6.0, (y + float(fret - 12 * col)) / 12.0));
            }
        """.trimIndent()
        private var mProgram : Int = -1
        fun initialize() {
            val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
            mProgram = makeProgramFromShaders(vertexShader, fragmentShader)
        }
    }
    override fun internalDraw(time : Float, scrollSpeed : Float) {
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

class Anchor(
    anchor : Event.Anchor,
    var lastAnchorTime : Float
) : EventShape<Event.Anchor>(vertexCoords, drawOrder, mProgram, anchor) {
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
            $beltColorGLSL
            $bumpColorGLSL
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
    override val endTime : Float get() = lastAnchorTime
    override val sortLevel = SortLevel.Tab
    override fun internalDraw(time : Float, scrollSpeed : Float) {
        val timeHandle = glGetUniformLocation(mProgram, "uTime")
        val movedTime = max(event.time, time)
        glUniform2f(timeHandle,
            (time - movedTime) * scrollSpeed,
            (lastAnchorTime - movedTime) * scrollSpeed)
        val fretHandle = glGetUniformLocation(mProgram, "uFret")
        glUniform2i(fretHandle, event.fret.toInt(), event.width.toInt())
        super.internalDraw(time, scrollSpeed)
    }
}

class SongScroller(
    private val song : List<Event>,
    private val horizon : Float
) {
    private var time : Float = 0F
    private var position : Int = 0
    private var lastAnchor = Anchor(Event.Anchor(0f, 1 , 4), horizon)
    private var events : ArrayList<EventShape<Event>> = arrayListOf(lastAnchor)

    val activeEvents : List<EventShape<Event>> get() = events
    val currentTime : Float get() = time

    fun advance(t : Float, onRemove : (Event) -> Unit) {
        time += t

        lastAnchor.lastAnchorTime = time + horizon

        val it = events.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e.endTime < time) {
                onRemove(e.event)
                it.remove()
            }
        }

        while (position < song.size && song[position].time < time + horizon) {
            when (val event = song[position++]) {
                is Event.Anchor -> {
                    lastAnchor.lastAnchorTime = event.time
                    lastAnchor = Anchor(event, time + horizon)
                    events.add(lastAnchor)
                }
                is Event.Note ->
                    if (event.fret > 0)
                        events.add(Note(event))
                    else
                        events.add(EmptyStringNote(event, lastAnchor.event))
                is Event.Beat ->
                    events.add(Beat(event, lastAnchor.event))
            }
        }
    }
}

class MyGLRenderer(val song : List<Event>, private val context : Context) : GLSurfaceView.Renderer {
    private lateinit var neck : Neck
    private lateinit var textures : Textures
    private lateinit var fretNumbers : FretNumbers
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private var lastFrameTime : Long = 0
    private var scrollSpeed : Float = 13f
    private lateinit var scroller : SongScroller
    private var awayAnchor : Event.Anchor = Event.Anchor(0f, 1 , 4)
    private var finalAnchor : Event.Anchor = awayAnchor
    private var leftFret : Int = 0
    private var rightFret : Int = 0
    private var eyeX : Float = 2f
    private var eyeY : Float = 1.2f
    private var eyeZ : Float = 3f
    private val sounds : SoundPool = SoundPool.Builder().run {
        setAudioAttributes(AudioAttributes.Builder().run {
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            build()
        })
        setMaxStreams(2)
        build()
    }
    private val metronome1 = sounds.load(context, R.raw.metronome1, 1)
    private val metronome2 = sounds.load(context, R.raw.metronome2, 1)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        //glEnable(GL_DEPTH_TEST)
        Neck.initialize()
        FretNumbers.initialize()
        Anchor.initialize()
        Note.initialize()
        EmptyStringNote.initialize()
        Beat.initialize()
        textures = Textures(context)
        neck = Neck()
        fretNumbers = FretNumbers(textures)
        lastFrameTime = SystemClock.elapsedRealtime()
        scroller = SongScroller(song, 40f / scrollSpeed)
    }

    override fun onDrawFrame(gl: GL10?) {
        val currentTime = SystemClock.elapsedRealtime()
        val deltaTime = currentTime - lastFrameTime
        lastFrameTime = currentTime

        scroller.advance(deltaTime / 1000.0F) { evt : Event ->
            when (evt) {
                is Event.Beat ->
                    sounds.play(if (evt.measure >= 0) metronome2 else metronome1,
                        1f, 1f, 0, 0, 1f)
                is Event.Anchor ->
                    finalAnchor = evt
            }
        }

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY * 2.1f, eyeZ, eyeX, eyeY * 1.1f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        leftFret = 24
        rightFret = 1
        fun updateFretBounds(evt : Event.Anchor) {
            if (evt.fret < leftFret) leftFret = evt.fret.toInt()
            if (evt.fret + evt.width > rightFret) rightFret = evt.fret + evt.width
        }
        for (evt in scroller.activeEvents.sortedWith(compareByDescending(EventShape<Event>::endTime).thenBy { it.sortLevel.level() })) {
            evt.draw(vPMatrix, scroller.currentTime, scrollSpeed)
            when (evt.event) {
                is Event.Anchor -> {
                    updateFretBounds(evt.event)
                }
            }
        }

        val targetEyeX = (leftFret + rightFret)/2.0f - 1f
        val targetEyeY = (rightFret - leftFret + 2)/6.0f*1.2f
        val targetEyeZ = (rightFret - leftFret + 2)/6.0f*3f
        eyeX = 0.02f*targetEyeX + 0.98f*eyeX
        eyeY = 0.02f*targetEyeY + 0.98f*eyeY
        eyeZ = 0.02f*targetEyeZ + 0.98f*eyeZ

        neck.draw(vPMatrix, scroller.currentTime, scrollSpeed)
        fretNumbers.draw(vPMatrix, scroller.currentTime, scrollSpeed)
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
