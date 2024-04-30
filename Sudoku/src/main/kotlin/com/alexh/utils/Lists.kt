package com.alexh.utils

fun <T> List<T>.get2d(
    rowIndex: Int,
    colIndex: Int,
    cols: Int
): T {
    val actualIndex = rowIndex * cols + colIndex

    return this[actualIndex]
}

fun <T> List<T>.unflatten(
    rowLength: Int,
): List<List<T>> {
    val matrix = mutableListOf<MutableList<T>>()
    val iter = this.iterator()

    while (iter.hasNext()) {
        val row = mutableListOf<T>()

        repeat(rowLength) {
            val value = iter.next()

            row.add(value)
        }

        matrix.add(row)
    }

    return matrix
}
