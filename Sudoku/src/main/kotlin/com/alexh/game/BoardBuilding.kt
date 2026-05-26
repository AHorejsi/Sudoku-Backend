package com.alexh.game

import com.alexh.utils.get2d
import com.alexh.utils.up

internal fun buildBoard(dimension: Dimension, games: Set<Game>): SudokuGraph {
    val neighborhoods = initializeNeighborhoods(dimension, games)
    val graph = SudokuGraph(neighborhoods, dimension.length)

    return graph
}

private fun initializeNeighborhoods(dimension: Dimension, games: Set<Game>): List<SudokuNode> {
    val length = dimension.length
    val neighborhoods = ArrayList<SudokuNode>(length * length)

    val boxRows = dimension.boxRows
    val boxCols = dimension.boxCols

    makeRegularNeighborhoods(neighborhoods, length, boxRows, boxCols)

    if (Game.HYPER in games) {
        makeRegularHyperNeighborhoods(neighborhoods, length, boxRows, boxCols)
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

    makeNodes(neighborhoods, range)
    makeRegularConnections(neighborhoods, range, length, boxRows, boxCols)
}

private fun makeNodes(neighborhoods: MutableList<SudokuNode>, range: IntRange) {
    for (rowIndex in range) {
        for (colIndex in range) {
            val newNode = SudokuNode()

            neighborhoods.add(newNode)
        }
    }
}

private fun makeRegularConnections(
    neighborhoods: List<SudokuNode>,
    range: IntRange,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    for (rowIndex in range) {
        for (colIndex in range) {
            includeRow(rowIndex, colIndex, range, neighborhoods, length)
            includeCol(rowIndex, colIndex, range, neighborhoods, length)
            includeBox(rowIndex, colIndex, range, neighborhoods, length, boxRows, boxCols)
        }
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

private fun includeCol(
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

private fun includeBox(
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

    for (neighborRowIndex in rows) {
        for (neighborColIndex in cols) {
            val other = neighborhoods.get2d(neighborRowIndex, neighborColIndex, length)

            if (other !== current) {
                current.addToBox(other)
            }
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

    val rowStartPoints = 1 until endIndex step (boxRows + 1)
    val colStartPoints = 1 until endIndex step (boxCols + 1)

    for (startRowIndex in rowStartPoints) {
        for (startColIndex in colStartPoints) {
            val rows = startRowIndex up boxRows
            val cols = startColIndex up boxCols

            makeHyperBoxes(neighborhoods, length, rows, cols)
        }
    }
}

private fun makeHyperBoxes(
    neighborhoods: List<SudokuNode>,
    length: Int,
    rowRange: IntRange,
    colRange: IntRange
) {
    for (rowIndex in rowRange) {
        for (colIndex in colRange) {
            val node = neighborhoods.get2d(rowIndex, colIndex, length)

            makeIndividualHyperBox(node, neighborhoods, length, rowRange, colRange)
        }
    }
}

private fun makeIndividualHyperBox(
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
