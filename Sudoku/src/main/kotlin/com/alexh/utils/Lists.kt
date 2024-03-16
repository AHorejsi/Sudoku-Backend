package com.alexh.utils

fun <T> get2d(rowIndex: Int, colIndex: Int, cols: Int, list: List<T>): T {
    val actualIndex = rowIndex * cols + colIndex

    return list[actualIndex]
}