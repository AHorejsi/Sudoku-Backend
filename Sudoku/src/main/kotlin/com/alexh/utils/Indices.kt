package com.alexh.utils

import kotlinx.serialization.Serializable

@Serializable
data class Position(
    val rowIndex: Int,
    val colIndex: Int
) {
    fun up(): Position = Position(this.rowIndex - 1, this.colIndex)

    fun down(): Position = Position(this.rowIndex + 1, this.colIndex)

    fun left(): Position = Position(this.rowIndex, this.colIndex - 1)

    fun right(): Position = Position(this.rowIndex, this.colIndex + 1)
}

infix fun Int.up(
    amount: Int
): IntRange =
    this until (this + amount)
