package com.alexh.utils

fun <TElement> List<TElement>.get2d(rowIndex: Int, colIndex: Int, cols: Int): TElement {
    val actualIndex = rowIndex * cols + colIndex

    return this[actualIndex]
}

fun <TElement> List<TElement>.get2d(pos: Position, cols: Int): TElement {
    return this.get2d(pos.rowIndex, pos.colIndex, cols)
}

fun <TElement> List<TElement>.unflatten(rowLength: Int): List<List<TElement>> {
    if (0 != this.size % rowLength) {
        throw IllegalArgumentException("Cannot break up list with given row length")
    }

    val matrix = ArrayList<List<TElement>>(this.size / rowLength)
    val iter = this.iterator()

    while (iter.hasNext()) {
        val row = ArrayList<TElement>(rowLength)

        repeat(rowLength) {
            val value = iter.next()

            row.add(value)
        }

        matrix.add(row)
    }

    return matrix
}
