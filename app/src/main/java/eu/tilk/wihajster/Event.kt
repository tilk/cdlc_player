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
        val tremolo : Boolean = false,
        val linked : Boolean = false,
        val vibrato : Short = 0,
        val bend : List<Pair<Float, Float>> = ArrayList()
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