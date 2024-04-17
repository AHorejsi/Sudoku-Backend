package com.alexh.utils

import kotlinx.serialization.Serializable

@Serializable
data class Position(
    val rowIndex: Int,
    val colIndex: Int
)

infix fun Int.up(
    amount: Int
): IntRange = this until (this + amount)
