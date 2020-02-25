package eu.tilk.wihajster

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle

class MainActivity : Activity() {

    private lateinit var glView : GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glView = MyGLSurfaceView(this)
        setContentView(glView)
    }
}
