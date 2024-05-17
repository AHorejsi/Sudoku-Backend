package com.alexh.utils

import java.lang.IllegalArgumentException

fun <T> List<T>.get2d(
    rowIndex: Int,
    colIndex: Int,
    cols: Int
): T {
    val actualIndex = rowIndex * cols + colIndex

    return this[actualIndex]
}

fun <T> MutableList<T>.set2d(
    rowIndex: Int,
    colIndex: Int,
    cols: Int,
    elem: T
) {
    val actualIndex = rowIndex * cols + colIndex

    this[actualIndex] = elem
}

fun <T> List<T>.unflatten(
    rowLength: Int,
): List<List<T>> {
    if (0 != this.size % rowLength) {
        throw IllegalArgumentException("Cannot break up list with the given row length")
    }

    val matrix = ArrayList<MutableList<T>>(this.size / rowLength)
    val iter = this.iterator()

    while (iter.hasNext()) {
        val row = ArrayList<T>(rowLength)

        repeat(rowLength) {
            val value = iter.next()

            row.add(value)
        }

        matrix.add(row)
    }

    return matrix
}
