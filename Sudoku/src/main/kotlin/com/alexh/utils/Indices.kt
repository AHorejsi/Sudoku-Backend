package com.alexh.utils

data class Position(val rowIndex: Int, val colIndex: Int) {
    companion object {
        val INVALID = Position(-1, -1)
    }
}

infix fun Int.up(amount: Int): IntRange = this until (this + amount)
