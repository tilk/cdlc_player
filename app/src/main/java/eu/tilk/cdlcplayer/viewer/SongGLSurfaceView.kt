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

package eu.tilk.cdlcplayer.viewer

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import eu.tilk.cdlcplayer.SongViewModel
import eu.tilk.cdlcplayer.song.Song2014

class SongGLSurfaceView(context : Context, viewModel : SongViewModel) : GLSurfaceView(context) {
    private val renderer : SongGLRenderer
    private val gestureDetector : GestureDetector

    init {
        setEGLContextClientVersion(3)
        renderer = SongGLRenderer(
            context,
            viewModel
        )
        gestureDetector = GestureDetector(context, renderer.gestureListener)
        gestureDetector.setIsLongpressEnabled(false)
        gestureDetector.setOnDoubleTapListener(renderer.gestureListener)
        setRenderer(renderer)
    }

    override fun onTouchEvent(event : MotionEvent?) : Boolean {
        return gestureDetector.onTouchEvent(event!!)
    }

    fun nextBeats() = renderer.nextBeats()
}
