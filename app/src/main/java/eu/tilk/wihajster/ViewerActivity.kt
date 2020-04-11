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

package eu.tilk.wihajster

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import eu.tilk.wihajster.psarc.PSARCReader
import eu.tilk.wihajster.viewer.SongGLSurfaceView

class ViewerActivity : Activity() {

    private lateinit var glView : GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val song = resources.assets.openFd("songs/numberbeast_p.psarc").use {
            val psarc = PSARCReader(it.createInputStream())
            val file = psarc.inflateManifest("manifests/songs_dlc_numberbeast/numberbeast_lead.json")
            psarc.inflateSng("songs/bin/generic/numberbeast_lead.sng",
                file.entries.values.first().values.first())
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        glView = SongGLSurfaceView(this, song)
        setContentView(glView)
    }
}
