package com.alexh.utils

fun boxIndex(rowIndex: Int, colIndex: Int, boxRows: Int, boxCols: Int): Int =
    rowIndex / boxRows * boxRows + colIndex / boxCols

fun actualIndex(rowIndex: Int, colIndex: Int, cols: Int): Int =
    rowIndex * cols + colIndex
