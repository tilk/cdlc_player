package eu.tilk.wihajster

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.WindowManager

class MainActivity : Activity() {

    private lateinit var glView : GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        glView = MyGLSurfaceView(this)
        setContentView(glView)
    }
}
