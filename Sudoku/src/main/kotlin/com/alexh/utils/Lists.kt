package com.alexh.utils

fun <T> List<T>.get2d(
    rowIndex: Int,
    colIndex: Int,
    cols: Int
): T {
    val actualIndex = rowIndex * cols + colIndex

    return this[actualIndex]
}