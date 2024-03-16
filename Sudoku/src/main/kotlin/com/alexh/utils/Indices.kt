package com.alexh.utils

data class Position(val rowIndex: Int, val colIndex: Int) {
    companion object {
        val INVALID = Position(-1, -1)
    }
}

fun checkBounds(rowIndex: Int, colIndex: Int, rows: Int, cols: Int) {
    if (rowIndex < 0 || rowIndex >= rows || colIndex < 0 || colIndex >= cols) {
        throw IndexOutOfBoundsException("RowIndex = $rowIndex, Rows = $rows, ColIndex = $colIndex, Cols = $cols")
    }
}

infix fun Int.up(amount: Int): IntRange = this until (this + amount)
