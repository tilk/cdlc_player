/*
 *     Copyright (C) 2020  Marek Materzok
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package eu.tilk.wihajster.shapes

import android.util.Log
import java.nio.IntBuffer
import android.opengl.GLES31.*

const val COORDS_PER_VERTEX = 3

const val logisticGLSL = """
float logistic(float x) {
    return 0.5 + 0.5 * tanh(x);
}
"""
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
const val specialFretsGLSL = """
bool isOneDotFret(int x) {
    return x % 2 == 1 && (x + 1) % 12 > 2; 
}
bool isTwoDotFret(int x) {
    return x > 0 && x % 12 == 0;
}
bool isSpecialFret(int x) {
    return isOneDotFret(x) || isTwoDotFret(x);
}
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