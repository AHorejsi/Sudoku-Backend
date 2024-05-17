package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.get2d
import com.alexh.utils.up

internal fun initializeBoard(
    info: MakeSudokuCommand
): Pair<List<SudokuNode>, Set<Box>> {
    val length = info.dimension.length

    val neighborhoods = ArrayList<SudokuNode>(length * length)
    initializeNeighborhoods(info, length, neighborhoods)

    val boxes = HashSet<Box>(length * 2)
    initializeBoxes(neighborhoods, length, boxes)

    return neighborhoods to boxes
}

private fun initializeNeighborhoods(
    info: MakeSudokuCommand,
    length: Int,
    neighborhoods: MutableList<SudokuNode>
) {
    val boxRows = info.dimension.boxRows
    val boxCols = info.dimension.boxCols

    makeRegularNeighborhoods(neighborhoods, length, boxRows, boxCols)

    if (Game.HYPER in info.games) {
        makeRegularHyperNeighborhoods(neighborhoods, length, boxRows, boxCols)
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
            val place = Position(rowIndex, colIndex)
            val newNode = SudokuNode(length, place)

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
            current.addToRowSet(other)
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
            current.addToColSet(other)
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
    val current = neighborhoods.get2d(currentRowIndex, currentColIndex, length)

    for (neighborRowIndex in startRowIndex up boxRows) {
        for (neighborColIndex in startColIndex up boxCols) {
            val other = neighborhoods.get2d(neighborRowIndex, neighborColIndex, length)

            if (other !== current) {
                current.addToBoxSet(other)
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
                current.addToHyperSet(other)
            }
        }
    }
}

private fun initializeBoxes(
    neighborhoods: List<SudokuNode>,
    length: Int,
    boxSet: MutableSet<Box>
) {
    val seen = HashSet<SudokuNode>(2 * length * length)

    for (node in neighborhoods) {
        val box = makeBox(node, length, seen)

        box?.let {
            boxSet.add(it)
        }
    }

    seen.clear()

    for (node in neighborhoods) {
        val hyperBox = makeHyperBox(node, length, seen)

        hyperBox?.let {
            boxSet.add(it)
        }
    }
}

private fun makeBox(
    node: SudokuNode,
    length: Int,
    seen: MutableSet<SudokuNode>
): Box? {
    if (node in seen) {
        return null
    }

    val positions = HashSet<Position>(length)

    for (neighbor in node.box) {
        positions.add(neighbor.place)
    }

    positions.add(node.place)

    return Box(false, positions)
}

private fun makeHyperBox(
    node: SudokuNode,
    length: Int,
    seen: MutableSet<SudokuNode>
): Box? {
    if (node in seen || !node.hyper.any()) {
        return null
    }

    val positions = HashSet<Position>(length)

    for (neighbor in node.hyper) {
        positions.add(neighbor.place)
    }

    positions.add(node.place)

    return Box(true, positions)
}
