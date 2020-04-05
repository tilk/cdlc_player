package eu.tilk.wihajster

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.WindowManager
import eu.tilk.wihajster.psarc.PSARC
import loggersoft.kotlin.streams.StreamAdapter

class MainActivity : Activity() {

    private lateinit var glView : GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val psarc = resources.assets.openFd("songs/numberbeast_p.psarc").use {
            PSARC(it.createInputStream())
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        glView = MyGLSurfaceView(this)
        setContentView(glView)
    }
}
