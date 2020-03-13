package eu.tilk.wihajster

sealed class Event {
    abstract val time : Float
    val endTime : Float get() = time

    data class Note(
        override val time : Float,
        val fret : Byte,
        val string : Byte
    ) : Event()

    data class Beat(
        override val time : Float
    ) : Event()
}