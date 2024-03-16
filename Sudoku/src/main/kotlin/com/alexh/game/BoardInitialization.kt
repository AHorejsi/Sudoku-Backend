package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.get2d
import com.alexh.utils.up
import kotlin.random.Random

internal fun initializeBoard(info: MakeSudokuCommand): List<SudokuCellNode> {
    val length = info.dimension.length
    val rand = info.random

    val neighborhoods = mutableListOf<MutableSudokuCellNode>()

    initializeBoardHelper(info, length, neighborhoods, rand)

    return neighborhoods
}

private fun initializeBoardHelper(
    info: MakeSudokuCommand,
    length: Int,
    neighborhoods: MutableList<MutableSudokuCellNode>,
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
    neighborhoods: MutableList<MutableSudokuCellNode>,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    val range = 0 until length

    makeNodes(neighborhoods, range, length)
    makeRegularConnections(neighborhoods, range, length, boxRows, boxCols)
}

private fun makeNodes(neighborhoods: MutableList<MutableSudokuCellNode>, range: IntRange, length: Int) {
    for (rowIndex in range) {
        for (colIndex in range) {
            val position = Position(rowIndex, colIndex)
            val neighbors = HashSet<MutableSudokuCellNode>(length)
            val node = MutableSudokuCellNode(position, neighbors)

            neighborhoods.add(node)
        }
    }
}

private fun makeRegularConnections(
    neighborhoods: MutableList<MutableSudokuCellNode>,
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
    neighborhoods: MutableList<MutableSudokuCellNode>,
    length: Int
) {
    val node = get2d(currentRowIndex, currentColIndex, length, neighborhoods)

    for (neighborColIndex in range) {
        val neighborNode = get2d(currentRowIndex, neighborColIndex, length, neighborhoods)

        if (neighborNode !== node) {
            node.neighbors.add(neighborNode)
        }
    }
}

private fun includeColumn(
    currentRowIndex: Int,
    currentColIndex: Int,
    range: IntRange,
    neighborhoods: MutableList<MutableSudokuCellNode>,
    length: Int
) {
    val node = get2d(currentRowIndex, currentColIndex, length, neighborhoods)

    for (neighborRowIndex in range) {
        val neighborNode = get2d(neighborRowIndex, currentColIndex, length, neighborhoods)

        if (neighborNode !== node) {
            node.neighbors.add(neighborNode)
        }
    }
}

private fun includeBox(
    currentRowIndex: Int,
    currentColIndex: Int,
    range: IntRange,
    neighborhoods: MutableList<MutableSudokuCellNode>,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    val startRowIndex = findStartOfBox(currentRowIndex, range, boxCols)
    val startColIndex = findStartOfBox(currentColIndex, range, boxRows)
    val node = get2d(currentRowIndex, currentColIndex, length, neighborhoods)

    for (neighborRowIndex in startRowIndex up boxRows) {
        for (neighborColIndex in startColIndex up boxCols) {
            val neighborNode = get2d(neighborRowIndex, neighborColIndex, length, neighborhoods)

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
    neighborhoods: List<MutableSudokuCellNode>,
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
    neighborhoods: List<MutableSudokuCellNode>,
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
            val node = get2d(rowIndex, colIndex, length, neighborhoods)

            makeIndividualHyperBox(node, neighborhoods, length, rowRange, colRange)
        }
    }
}

private fun makeIndividualHyperBox(
    current: MutableSudokuCellNode,
    neighborhoods: List<MutableSudokuCellNode>,
    length: Int,
    rowRange: IntRange,
    colRange: IntRange
) {
    for (rowIndex in rowRange) {
        for (colIndex in colRange) {
            val other = get2d(rowIndex, colIndex, length, neighborhoods)

            if (current !== other) {
                current.neighbors.add(other)
            }
        }
    }
}
