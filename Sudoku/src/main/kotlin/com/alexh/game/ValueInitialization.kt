package com.alexh.game

import com.alexh.utils.get2d
import kotlin.random.Random

internal fun initializeValues(neighborhoods: List<SudokuCellNode>, info: MakeSudokuCommand) {
    val rand = info.random
    val length = info.dimension.length
    val legal = (1 .. length).asSequence()

    initializeValuesHelper(neighborhoods, length, rand, legal, 0, 0)
}

private fun initializeValuesHelper(
    neighborhoods: List<SudokuCellNode>,
    length: Int,
    rand: Random,
    legal: Sequence<Int>,
    rowIndex: Int,
    colIndex: Int
): Boolean {
    if (rowIndex == length) {
        return true
    }

    val node = get2d(rowIndex, colIndex, length, neighborhoods)
    val (nextRowIndex, nextColIndex) = nextPosition(rowIndex, colIndex, length)

    for (value in legal.shuffled(rand)) {
        if (isSafe(value, node)) {
            node.value = value

            if (initializeValuesHelper(neighborhoods, length, rand, legal, nextRowIndex, nextColIndex)) {
                return true
            }

            node.value = null
        }
    }

    return false
}

private fun isSafe(value: Int, node: SudokuCellNode): Boolean {
    for (neighbor in node.neighbors) {
        if (value == neighbor.value) {
            return false
        }
    }

    return true
}

private fun nextPosition(rowIndex: Int, colIndex: Int, length: Int): Pair<Int, Int> {
    var nextRowIndex = rowIndex
    var nextColIndex = colIndex

    ++nextColIndex

    if (length == nextColIndex) {
        ++nextRowIndex
        nextColIndex = 0
    }

    return Pair(nextRowIndex, nextColIndex)
}
