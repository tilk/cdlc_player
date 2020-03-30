package eu.tilk.wihajster.shapes

import eu.tilk.wihajster.Event
import eu.tilk.wihajster.SortLevel

abstract class EventShape<out T : Event>(
    vertexCoords : FloatArray,
    drawOrder : ShortArray,
    mProgram : Int,
    val event : T
) : StaticShape(vertexCoords, drawOrder, mProgram) {
    open val endTime : Float get() = event.time
    abstract val sortLevel : SortLevel
}