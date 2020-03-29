package eu.tilk.wihajster

sealed class SortLevel {
    abstract fun level() : Int

    abstract class Const(private val level : Int) : SortLevel() {
        override fun level() = level
    }

    object Tab : Const(-2)
    object Beat : Const(-1)
    data class String(val string : Int) : SortLevel() {
        override fun level() = string
    }
    object Chord : Const(7)
}