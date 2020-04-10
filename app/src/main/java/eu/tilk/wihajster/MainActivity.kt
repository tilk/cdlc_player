package eu.tilk.wihajster

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import eu.tilk.wihajster.psarc.PSARCReader
import java.nio.charset.Charset

class MainActivity : Activity() {

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
        glView = MyGLSurfaceView(this, song)
        setContentView(glView)
    }
}
