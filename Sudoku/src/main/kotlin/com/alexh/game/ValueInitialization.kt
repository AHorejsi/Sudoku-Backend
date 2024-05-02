package com.alexh.game

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

    val unassigned = neighborhoods.asSequence().filter{ null === it.value }.toMutableList()
    val legalMap = shuffleValues(neighborhoods, length, rand, legal)

    initializeValuesHelper2(unassigned, legalMap)
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
): Map<SudokuNode, Sequence<Int>> {
    val range = 0 until length
    val legalMap = HashMap<SudokuNode, Sequence<Int>>()

    for (rowIndex in range) {
        for (colIndex in range) {
            val node = neighborhoods.get2d(rowIndex, colIndex, length)

            if (null === node.value) {
                val shuffled = legal.shuffled(rand)

                legalMap[node] = shuffled
            }
        }
    }

    return legalMap
}

private fun initializeValuesHelper2(
    unassigned: MutableList<SudokuNode>,
    legalMap: Map<SudokuNode, Sequence<Int>>
): Boolean {
    if (unassigned.isEmpty()) {
        return true
    }

    val node = unassigned.removeLast()

    for (value in legalMap.getValue(node)) {
        if (isSafe(value, node)) {
            node.value = value

            if (initializeValuesHelper2(unassigned, legalMap)) {
                return true
            }

            node.value = null
        }
    }

    unassigned.add(node)

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
