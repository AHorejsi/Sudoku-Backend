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
    val legal = 1 .. length

    initializeValuesHelper1(neighborhoods, dimension, rand, legal, games)

    val unassigned = neighborhoods.filter{ null === it.value }.toMutableList()
    val legalMap = shuffleValues(neighborhoods, length, rand, legal)

    initializeValuesHelper2(unassigned, legalMap)
}

private fun initializeValuesHelper1(
    neighborhoods: List<SudokuNode>,
    dimension: Dimension,
    rand: Random,
    legal: IntRange,
    games: Set<Game>
) {
    if (Game.HYPER in games) {
        fillBox(neighborhoods, dimension.length, 1 up dimension.boxRows, 1 up dimension.boxCols, legal, rand)
    }
    else if (games.isEmpty()) {
        fillRegularDiagonal(neighborhoods, dimension, rand, legal)
    }
}

private fun fillRegularDiagonal(
    neighborhoods: List<SudokuNode>,
    dimension: Dimension,
    rand: Random,
    legal: IntRange
) {
    val length = dimension.length
    val boxRows = dimension.boxRows
    val boxCols = dimension.boxCols

    val rowRange = 0 until length step boxRows
    val colRange = 0 until length step boxCols

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
    legal: IntRange,
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
    legal: IntRange
): Map<SudokuNode, List<Int>> {
    val range = 0 until length
    val legalMap = HashMap<SudokuNode, List<Int>>()

    for (rowIndex in range) {
        for (colIndex in range) {
            val node = neighborhoods.get2d(rowIndex, colIndex, length)

            if (null === node.value) {
                legalMap[node] = legal.shuffled(rand)
            }
        }
    }

    return legalMap
}

private fun initializeValuesHelper2(
    unassigned: MutableList<SudokuNode>,
    legalMap: Map<SudokuNode, List<Int>>
): Boolean {
    if (unassigned.isEmpty()) {
        return true
    }

    val node = unassigned.removeLast()
    val valueList = legalMap.getValue(node)

    for (value in valueList) {
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
    for (neighbor in node.all) {
        if (value == neighbor.value) {
            return false
        }
    }

    return true
}
