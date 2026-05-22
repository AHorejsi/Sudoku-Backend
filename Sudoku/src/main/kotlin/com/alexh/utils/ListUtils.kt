package com.alexh.utils

fun <TElement> List<TElement>.get2d(rowIndex: Int, colIndex: Int, cols: Int): TElement {
    val actualIndex = rowIndex * cols + colIndex

    return this[actualIndex]
}
