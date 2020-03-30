package eu.tilk.wihajster.shapes

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