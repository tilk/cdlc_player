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

package eu.tilk.cdlcplayer.shapes

import android.opengl.GLES31.*
import android.util.Log
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

abstract class Shape(
    private val companion : CompanionBase
) {
    open class CompanionBase(
        private val vertexShaderCode : String,
        private val fragmentShaderCode : String
    ) {
        @Suppress("LeakingThis")
        abstract inner class GLCache {
            private var state = -1
            fun clear() { state = -1 }
            protected abstract fun reload() : Int
            val value : Int get() {
                if (state == -1) state = reload()
                return state
            }
            init {
                caches.add(this)
            }
        }
        inner class GLUniformCache(private val name : String) : GLCache() {
            override fun reload() = glGetUniformLocation(mProgram, name)
        }
        inner class GLAttribCache(private val name : String) : GLCache() {
            override fun reload() = glGetAttribLocation(mProgram, name)
        }
        companion object {
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
            const val laneColorsGLSL = """
                const vec3 laneColors[2] = vec3[](
                    vec3(0.573, 0.573, 0.573),
                    vec3(1.0, 0.94, 0.0)
                );    
            """
        }
        internal var mProgram : Int = -1
        private val caches = mutableListOf<GLCache>()

        private fun loadShader(type : Int, shaderCode : String): Int {
            return glCreateShader(type).also {
                glShaderSource(it, shaderCode)
                glCompileShader(it)
            }
        }

        private fun checkProgram(n : Int) {
            val buf = IntBuffer.allocate(1)
            glGetProgramiv(n, GL_LINK_STATUS, buf)
            if (buf[0] != GL_TRUE) {
                Log.e("program", glGetProgramInfoLog(n))
            }
        }

        private fun makeProgramFromShaders(vertexShader : Int, fragmentShader : Int) : Int {
            return glCreateProgram().also {
                glAttachShader(it, vertexShader)
                glAttachShader(it, fragmentShader)
                glLinkProgram(it)
                checkProgram(it)
            }
        }

        fun reset() {
            for (cache in caches) cache.clear()
        }

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

        internal val uMVPMatrix = GLUniformCache("uMVPMatrix")
        internal val vPosition  = GLAttribCache("vPosition")
    }
    protected abstract val vertexBuffer : FloatBuffer
    protected abstract val drawListBuffer : ShortBuffer
    protected abstract val drawListSize : Int
    protected open val instances : Int = 1
    protected open fun internalDraw(time: Float, scrollSpeed : Float) {
        glEnableVertexAttribArray(companion.vPosition.value)
        glVertexAttribPointer(companion.vPosition.value,
            COORDS_PER_VERTEX, GL_FLOAT, false,
            COORDS_PER_VERTEX * 4, vertexBuffer)
        if (instances > 1)
            glDrawElementsInstanced(GL_TRIANGLES, drawListSize, GL_UNSIGNED_SHORT,
                drawListBuffer, instances)
        else
            glDrawElements(GL_TRIANGLES, drawListSize, GL_UNSIGNED_SHORT, drawListBuffer)
        glDisableVertexAttribArray(companion.vPosition.value)
    }
    private fun prepare(mvpMatrix : FloatArray) {
        glUseProgram(companion.mProgram)
        glUniformMatrix4fv(companion.uMVPMatrix.value, 1, false, mvpMatrix, 0)
    }
    fun draw(mvpMatrix : FloatArray, time: Float, scrollSpeed : Float) {
        prepare(mvpMatrix)
        internalDraw(time, scrollSpeed)
    }
}