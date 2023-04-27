package com.alexh.game

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
    var tentative: MutableList<String> = MutableList(length) { "" }
}
