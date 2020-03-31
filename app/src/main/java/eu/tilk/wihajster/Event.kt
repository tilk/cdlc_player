package eu.tilk.wihajster

sealed class Event {
    abstract val time : Float

    data class Note(
        override val time : Float,
        val fret : Byte,
        val string : Byte,
        val sustain : Float = 0f,
        val slideTo : Byte = -1,
        val slideUnpitchedTo : Byte = -1,
        val tremolo : Byte = -1
    ) : Event()

    data class Beat(
        override val time : Float,
        val measure : Short
    ) : Event()

    data class Anchor(
        override val time : Float,
        val fret : Byte,
        val width : Short
    ) : Event()
}