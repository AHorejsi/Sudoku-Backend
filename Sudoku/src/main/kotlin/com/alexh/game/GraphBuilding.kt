package com.alexh.game

import com.alexh.utils.*
import kotlin.random.Random

internal fun buildGraph(dimension: Dimension, games: Set<Game>, rand: Random): SudokuGraph {
    val neighborhoods = initializeNeighborhoods(dimension, games, rand)
    val graph = SudokuGraph(neighborhoods, dimension.length)

    return graph
}

private fun initializeNeighborhoods(dimension: Dimension, games: Set<Game>, rand: Random): List<SudokuNode> {
    val length = dimension.length
    val neighborhoods = makeNeighborhoodNodes(length)

    val boxRows = dimension.boxRows
    val boxCols = dimension.boxCols

    makeRegularNeighborhoods(neighborhoods, length, boxRows, boxCols)

    if (Game.HYPER in games) {
        makeRegularHyperNeighborhoods(neighborhoods, length, boxRows, boxCols)
    }

    return neighborhoods
}

private fun makeNeighborhoodNodes(length: Int): MutableList<SudokuNode> {
    val neighborhoods = ArrayList<SudokuNode>(length * length)

    val range = 0 until length

    for (rowIndex in range) {
        for (colIndex in range) {
            val pos = Position(rowIndex, colIndex)
            val newNode = SudokuNode(pos)

            neighborhoods.add(newNode)
        }
    }

    return neighborhoods
}

private fun makeRegularNeighborhoods(
    neighborhoods: MutableList<SudokuNode>,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    val range = 0 until length

    for ((rowIndex, colIndex) in range.pair(range)) {
        includeRow(rowIndex, colIndex, range, neighborhoods, length)
        includeColumn(rowIndex, colIndex, range, neighborhoods, length)
        includeRegularBox(rowIndex, colIndex, range, neighborhoods, length, boxRows, boxCols)
    }
}

private fun includeRow(
    currentRowIndex: Int,
    currentColIndex: Int,
    range: IntRange,
    neighborhoods: List<SudokuNode>,
    length: Int
) {
    val current = neighborhoods.get2d(currentRowIndex, currentColIndex, length)

    for (neighborColIndex in range) {
        val other = neighborhoods.get2d(currentRowIndex, neighborColIndex, length)

        if (other !== current) {
            current.addToRow(other)
        }
    }
}

private fun includeColumn(
    currentRowIndex: Int,
    currentColIndex: Int,
    range: IntRange,
    neighborhoods: List<SudokuNode>,
    length: Int
) {
    val current = neighborhoods.get2d(currentRowIndex, currentColIndex, length)

    for (neighborRowIndex in range) {
        val other = neighborhoods.get2d(neighborRowIndex, currentColIndex, length)

        if (other !== current) {
            current.addToColumn(other)
        }
    }
}

private fun includeRegularBox(
    currentRowIndex: Int,
    currentColIndex: Int,
    range: IntRange,
    neighborhoods: List<SudokuNode>,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    val startRowIndex = findStartOfBox(currentRowIndex, range, boxRows)
    val startColIndex = findStartOfBox(currentColIndex, range, boxCols)

    val rows = startRowIndex up boxRows
    val cols = startColIndex up boxCols

    val current = neighborhoods.get2d(currentRowIndex, currentColIndex, length)

    for ((neighborRowIndex, neighborColIndex) in rows.pair(cols)) {
        val other = neighborhoods.get2d(neighborRowIndex, neighborColIndex, length)

        if (other !== current) {
            current.addToBox(other)
        }
    }
}

private fun findStartOfBox(currentIndex: Int, range: IntRange, boxLength: Int): Int {
    var start = -1

    for (index in range step boxLength) {
        if (index > currentIndex) {
            break
        }

        start = index
    }

    return start
}

private fun makeRegularHyperNeighborhoods(
    neighborhoods: List<SudokuNode>,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    val endIndex = length - 1

    val rowStartingIndices = 1 until endIndex step (boxRows + 1)
    val colStartingIndices = 1 until endIndex step (boxCols + 1)

    for ((startRowIndex, startColIndex) in rowStartingIndices.pair(colStartingIndices)) {
        val rows = startRowIndex up boxRows
        val cols = startColIndex up boxCols

        for ((rowIndex, colIndex) in rows.pair(cols)) {
            val node = neighborhoods.get2d(rowIndex, colIndex, length)

            makeHyperBox(node, neighborhoods, length, rows, cols)
        }
    }
}

private fun makeHyperBox(
    current: SudokuNode,
    neighborhoods: List<SudokuNode>,
    length: Int,
    rowRange: IntRange,
    colRange: IntRange
) {
    for (rowIndex in rowRange) {
        for (colIndex in colRange) {
            val other = neighborhoods.get2d(rowIndex, colIndex, length)

            if (other !== current) {
                current.addToHyper(other)
            }
        }
    }
}
