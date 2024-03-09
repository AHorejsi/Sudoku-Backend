package com.alexh.game

import com.alexh.utils.actualIndex
import com.alexh.utils.up
import kotlin.math.sqrt
import kotlin.random.Random

internal fun initializeBoard(info: MakeSudokuCommand): MutableList<NeighborNode> {
    val length = info.dimension.length
    val rand = info.random

    val neighborhoods = mutableListOf<NeighborNode>()

    initializeBoardHelper(info, length, neighborhoods, rand)

    return neighborhoods
}

private fun initializeBoardHelper(
    info: MakeSudokuCommand,
    length: Int,
    neighborhoods: MutableList<NeighborNode>,
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
    neighborhoods: MutableList<NeighborNode>,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    val range = 0 until length

    makeNodes(neighborhoods, range)
    makeRegularConnections(neighborhoods, range, length, boxRows, boxCols)
}

private fun makeNodes(neighborhoods: MutableList<NeighborNode>, range: IntRange) {
    for (rowIndex in range) {
        for (colIndex in range) {
            val node = NeighborNode(null, mutableSetOf())

            neighborhoods.add(node)
        }
    }
}

private fun makeRegularConnections(
    neighborhoods: MutableList<NeighborNode>,
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
    neighborhoods: MutableList<NeighborNode>,
    length: Int
) {
    val node = neighborhoods[actualIndex(currentRowIndex, currentColIndex, length)]

    for (neighborColIndex in range) {
        val neighborNode = neighborhoods[actualIndex(currentRowIndex, neighborColIndex, length)]

        if (neighborNode !== node) {
            node.neighbors.add(neighborNode)
        }
    }
}

private fun includeColumn(
    currentRowIndex: Int,
    currentColIndex: Int,
    range: IntRange,
    neighborhoods: MutableList<NeighborNode>,
    length: Int
) {
    val node = neighborhoods[actualIndex(currentRowIndex, currentColIndex, length)]

    for (neighborRowIndex in range) {
        val neighborNode = neighborhoods[actualIndex(neighborRowIndex, currentColIndex, length)]

        if (neighborNode !== node) {
            node.neighbors.add(neighborNode)
        }
    }
}

private fun includeBox(
    currentRowIndex: Int,
    currentColIndex: Int,
    range: IntRange,
    neighborhoods: MutableList<NeighborNode>,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    val startRowIndex = findStartOfBox(currentRowIndex, range, boxCols)
    val startColIndex = findStartOfBox(currentColIndex, range, boxRows)
    val node = neighborhoods[actualIndex(currentRowIndex, currentColIndex, length)]

    for (neighborRowIndex in startRowIndex up boxRows) {
        for (neighborColIndex in startColIndex up boxCols) {
            val neighborNode = neighborhoods[actualIndex(neighborRowIndex, neighborColIndex, length)]

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
    neighborhoods: List<NeighborNode>,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {

}
