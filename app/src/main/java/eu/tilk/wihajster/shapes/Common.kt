package eu.tilk.wihajster.shapes

import android.util.Log
import java.nio.IntBuffer
import android.opengl.GLES31.*

const val COORDS_PER_VERTEX = 3

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