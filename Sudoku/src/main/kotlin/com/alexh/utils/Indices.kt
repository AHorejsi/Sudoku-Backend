package com.alexh.utils

import kotlinx.serialization.Serializable

@Serializable
data class Position (
    val rowIndex: Int,
    val colIndex: Int
) : Comparable<Position> {
    val up: Position
        get() = Position(this.rowIndex - 1, this.colIndex)

    val down: Position
        get() = Position(this.rowIndex + 1, this.colIndex)

    val left: Position
        get() = Position(this.rowIndex, this.colIndex - 1)

    val right: Position
        get() = Position(this.rowIndex, this.colIndex + 1)

    fun outOfBounds(length: Int): Boolean =
        outOfBounds(this.rowIndex, length) || outOfBounds(this.colIndex, length)

    override fun compareTo(other: Position): Int {
        val rowComp = this.rowIndex - other.rowIndex

        return if (0 != rowComp)
            rowComp
        else
            this.colIndex - other.colIndex
    }
}

fun outOfBounds(index: Int, length: Int): Boolean =
    index < 0 || index >= length

infix fun Int.up(amount: Int): IntRange =
    this until (this + amount)
