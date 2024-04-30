package com.alexh.utils

import kotlinx.serialization.Serializable

@Serializable
data class Position(
    val rowIndex: Int,
    val colIndex: Int
) {
    val up: Position
        get() = Position(this.rowIndex - 1, this.colIndex)

    val down: Position
        get() = Position(this.rowIndex + 1, this.colIndex)

    val left: Position
        get() = Position(this.rowIndex, this.colIndex - 1)

    val right: Position
        get() = Position(this.rowIndex, this.colIndex + 1)

    fun outOfBounds(length: Int): Boolean =
        this.rowIndex < 0 || this.colIndex < 0 || this.rowIndex >= length || this.colIndex >= length
}

infix fun Int.up(
    amount: Int
): IntRange = this until (this + amount)
