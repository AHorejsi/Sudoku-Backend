package com.alexh.utils

data class Position(val rowIndex: Int, val colIndex: Int)

fun boxIndex(rowIndex: Int, colIndex: Int, boxRows: Int, boxCols: Int): Int =
    rowIndex / boxRows * boxRows + colIndex / boxCols

fun actualIndex(rowIndex: Int, colIndex: Int, cols: Int): Int = rowIndex * cols + colIndex

fun checkBounds(rowIndex: Int, colIndex: Int, rows: Int, cols: Int) {
    if (rowIndex < 0 || rowIndex >= rows || colIndex < 0 || colIndex >= cols) {
        throw IndexOutOfBoundsException("RowIndex = $rowIndex, Rows = $rows, ColIndex = $colIndex, Cols = $cols")
    }
}

infix fun Int.up(amount: Int): IntRange = this until (this + amount)
