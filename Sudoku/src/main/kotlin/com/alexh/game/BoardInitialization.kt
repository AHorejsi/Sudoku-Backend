package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.get2d
import com.alexh.utils.up
import kotlin.random.Random

internal fun initializeBoard(
    info: MakeSudokuCommand
): List<SudokuNode> {
    val length = info.dimension.length
    val rand = info.random

    val neighborhoods = mutableListOf<SudokuNode>()

    initializeBoardHelper(info, length, neighborhoods, rand)

    return neighborhoods
}

private fun initializeBoardHelper(
    info: MakeSudokuCommand,
    length: Int,
    neighborhoods: MutableList<SudokuNode>,
    rand: Random
) {
    val games = info.games

    if (Game.JIGSAW in games) {
        //makeJigsawNeighborhoods()

        if (Game.HYPER in games) {
            //makeJigsawHyperNeighborhoods()
        }
    }
    else {
        val boxRows = info.dimension.boxRows
        val boxCols = info.dimension.boxCols

        makeRegularNeighborhoods(neighborhoods, length, boxRows, boxCols)

        if (Game.HYPER in games) {
            makeRegularHyperNeighborhoods(neighborhoods, length, boxRows, boxCols)
        }
    }
}

private fun makeRegularNeighborhoods(
    neighborhoods: MutableList<SudokuNode>,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    val range = 0 until length

    makeNodes(neighborhoods, range, length)
    makeRegularConnections(neighborhoods, range, length, boxRows, boxCols)
}

private fun makeNodes(
    neighborhoods: MutableList<SudokuNode>,
    range: IntRange,
    length: Int
) {
    for (rowIndex in range) {
        for (colIndex in range) {
            val neighbors = HashSet<SudokuNode>(length)
            val place = Position(rowIndex, colIndex)
            val node = SudokuNode(neighbors, place)

            neighborhoods.add(node)
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
            includeColumn(rowIndex, colIndex, range, neighborhoods, length)
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
    val node = neighborhoods.get2d(currentRowIndex, currentColIndex, length)

    for (neighborColIndex in range) {
        val neighborNode = neighborhoods.get2d(currentRowIndex, neighborColIndex, length)

        if (neighborNode !== node) {
            node.neighbors.add(neighborNode)
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
    val node = neighborhoods.get2d(currentRowIndex, currentColIndex, length)

    for (neighborRowIndex in range) {
        val neighborNode = neighborhoods.get2d(neighborRowIndex, currentColIndex, length)

        if (neighborNode !== node) {
            node.neighbors.add(neighborNode)
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
    val node = neighborhoods.get2d(currentRowIndex, currentColIndex, length)

    for (neighborRowIndex in startRowIndex up boxRows) {
        for (neighborColIndex in startColIndex up boxCols) {
            val neighborNode = neighborhoods.get2d(neighborRowIndex, neighborColIndex, length)

            if (neighborNode !== node) {
                node.neighbors.add(neighborNode)
            }
        }
    }
}

private fun findStartOfBox(
    currentIndex: Int,
    range: IntRange,
    boxLength: Int
): Int {
    var start = -1

    for (index in range step boxLength) {
        if (index > currentIndex) {
            break
        }
        else {
            start = index
        }
    }

    return start
}

private fun makeRegularHyperNeighborhoods(
    neighborhoods: List<SudokuNode>,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    val rowStartPoints = 1 until (length - 1) step (boxRows + 1)
    val colStartPoints = 1 until (length - 1) step (boxCols + 1)

    for (startRowIndex in rowStartPoints) {
        for (startColIndex in colStartPoints) {
            val endRowIndex = startRowIndex + boxRows
            val endColIndex = startColIndex + boxCols

            makeHyperBoxes(neighborhoods, length, startRowIndex, endRowIndex, startColIndex, endColIndex)
        }
    }
}

private fun makeHyperBoxes(
    neighborhoods: List<SudokuNode>,
    length: Int,
    startRowIndex: Int,
    endRowIndex: Int,
    startColIndex: Int,
    endColIndex: Int
) {
    val rowRange = startRowIndex until endRowIndex
    val colRange = startColIndex until endColIndex

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

            if (current !== other) {
                current.neighbors.add(other)
            }
        }
    }
}
