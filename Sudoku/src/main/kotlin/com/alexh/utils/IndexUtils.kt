package com.alexh.utils

import kotlinx.serialization.Serializable

@Serializable
data class Position(val rowIndex: Int, val colIndex: Int) : Comparable<Position> {
    val up: Position
        get() = Position(this.rowIndex - 1, this.colIndex)

    val down: Position
        get() = Position(this.rowIndex + 1, this.colIndex)

    val left: Position
        get() = Position(this.rowIndex, this.colIndex - 1)

    val right: Position
        get() = Position(this.rowIndex, this.colIndex + 1)

    override operator fun compareTo(other: Position): Int {
        val rowComp = this.rowIndex - other.rowIndex

        return if (0 != rowComp)
            rowComp
        else
            this.colIndex - other.colIndex
    }

    override fun equals(other: Any?): Boolean =
        other is Position && this.rowIndex == other.rowIndex && this.colIndex == other.colIndex

    override fun hashCode(): Int {
        val MODIFIER = 31

        var hashValue = MODIFIER
        hashValue += this.rowIndex.hashCode() * MODIFIER
        hashValue += this.colIndex.hashCode() * MODIFIER

        return hashValue
    }
}

fun outOfBounds(index: Int, length: Int): Boolean =
    index < 0 || index >= length

fun outOfBounds(pos: Position, length: Int): Boolean =
    outOfBounds(pos.rowIndex, length) || outOfBounds(pos.colIndex, length)

infix fun Int.up(amount: Int): IntRange =
    this until (this + amount)
