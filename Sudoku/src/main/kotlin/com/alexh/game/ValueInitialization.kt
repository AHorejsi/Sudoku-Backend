package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.get2d
import com.alexh.utils.up
import kotlin.random.Random

internal fun initializeValues(
    neighborhoods: List<SudokuNode>,
    info: MakeSudokuCommand
) {
    val rand = info.random
    val dimension = info.dimension
    val games = info.games

    val length = dimension.length
    val legal = (1 .. length).asSequence()

    initializeValuesHelper1(neighborhoods, dimension, rand, legal, games)

    val legalMap = shuffleValues(neighborhoods, length, rand, legal)
    val initial = Position(0, 0)

    initializeValuesHelper2(neighborhoods, length, legalMap, initial)
}

private fun initializeValuesHelper1(
    neighborhoods: List<SudokuNode>,
    dimension: Dimension,
    rand: Random,
    legal: Sequence<Int>,
    games: Set<Game>
) {
    if (Game.JIGSAW in games) {

    }
    else if (Game.HYPER in games) {
        fillBox(neighborhoods, dimension.length, 1 up dimension.boxRows, 1 up dimension.boxCols, legal, rand)
    }
    else {
        fillRegularDiagonal(neighborhoods, dimension, rand, legal)
    }
}

private fun fillRegularDiagonal(
    neighborhoods: List<SudokuNode>,
    dimension: Dimension,
    rand: Random,
    legal: Sequence<Int>
) {
    val length = dimension.length
    val boxRows = dimension.boxRows
    val boxCols = dimension.boxCols

    val rowRange = (0 until length step boxRows).asSequence()
    val colRange = (0 until length step boxCols).asSequence()

    for ((startRowIndex, startColIndex) in rowRange.zip(colRange)) {
        val rows = startRowIndex up boxRows
        val cols = startColIndex up boxCols

        fillBox(neighborhoods, length, rows, cols, legal, rand)
    }
}

private fun fillBox(
    neighborhoods: List<SudokuNode>,
    length: Int,
    rows: IntRange,
    cols: IntRange,
    legal: Sequence<Int>,
    rand: Random
) {
    val iter = legal.shuffled(rand).iterator()

    for (rowIndex in rows) {
        for (colIndex in cols) {
            val node = neighborhoods.get2d(rowIndex, colIndex, length)

            node.value = iter.next()
        }
    }
}

private fun shuffleValues(
    neighborhoods: List<SudokuNode>,
    length: Int,
    rand: Random,
    legal: Sequence<Int>
): Map<Position, Sequence<Int>> {
    val range = 0 until length
    val legalMap = HashMap<Position, Sequence<Int>>()

    for (rowIndex in range) {
        for (colIndex in range) {
            val node = neighborhoods.get2d(rowIndex, colIndex, length)

            if (null === node.value) {
                val pos = Position(rowIndex, colIndex)
                val shuffled = legal.shuffled(rand)

                legalMap[pos] = shuffled
            }
        }
    }

    return legalMap
}

private fun initializeValuesHelper2(
    neighborhoods: List<SudokuNode>,
    length: Int,
    legalMap: Map<Position, Sequence<Int>>,
    prev: Position
): Boolean {
    val next = nextPosition(prev, length, neighborhoods)

    if (length == next.rowIndex) {
        return true
    }

    val rowIndex = next.rowIndex
    val colIndex = next.colIndex
    val node = neighborhoods.get2d(rowIndex, colIndex, length)

    for (value in legalMap.getValue(next)) {
        if (isSafe(value, node)) {
            node.value = value

            if (initializeValuesHelper2(neighborhoods, length, legalMap, next)) {
                return true
            }

            node.value = null
        }
    }

    return false
}

private fun isSafe(
    value: Int,
    node: SudokuNode
): Boolean {
    for (neighbor in node.neighbors) {
        if (value == neighbor.value) {
            return false
        }
    }

    return true
}

private fun nextPosition(
    prev: Position,
    length: Int,
    neighborhoods: List<SudokuNode>
): Position {
    var nextRowIndex = prev.rowIndex
    var nextColIndex = prev.colIndex

    while (length != nextRowIndex) {
        val node = neighborhoods.get2d(nextRowIndex, nextColIndex, length)

        if (null === node.value) {
            break
        }

        ++nextColIndex

        if (length == nextColIndex) {
            ++nextRowIndex
            nextColIndex = 0
        }
    }

    return Position(nextRowIndex, nextColIndex)
}
