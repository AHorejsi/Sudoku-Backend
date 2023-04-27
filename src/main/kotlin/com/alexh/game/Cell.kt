package com.alexh.game

class TentativeList(length: Int) {
    private val values: MutableList<String> = MutableList(length) { "" }

    operator fun get(index: Int): String = this.values[index]

    operator fun set(index: Int, value: String) {
        this.values[index] = value
    }
}

internal class Cell(length: Int) {
    var value: Int? = null
        set(value) {
            if (!this.editable) {
                throw IllegalStateException()
            }

            field = value
        }
    var editable: Boolean = true
        set(editable) {
            if (null === this.value) {
                throw IllegalStateException()
            }

            field = editable
        }
    val tentative: TentativeList = TentativeList(length)
}
