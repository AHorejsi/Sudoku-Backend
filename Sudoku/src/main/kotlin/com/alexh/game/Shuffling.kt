package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.pair
import com.alexh.utils.thru
import com.alexh.utils.up
import kotlin.random.Random

internal fun shuffleBoard(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    dimension: Dimension,
    games: Set<Game>,
    rand: Random
) {
    val length = dimension.length
    val range = 0 until length
    val positions = range.pair(range)

    flipShuffle(graph, board, length, positions, rand)
    transposeShuffle(graph, board, positions, rand)
    rotateShuffle(graph, board, length, positions, rand)
    swapShuffle(graph, board, length, positions, rand)

    if (Game.HYPER !in games) {
        boxShuffle(graph, board, range, dimension, rand)
        innerBoxShuffle(graph, board, range, dimension, rand)
    }
}

private fun flipShuffle(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    length: Int,
    positions: Iterable<Position>,
    rand: Random
) {
    if (rand.nextBoolean()) {
        verticalFlip(graph, board, length, positions)
    }

    if (rand.nextBoolean()) {
        horizontalFlip(graph, board, length, positions)
    }
}

private fun verticalFlip(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    length: Int,
    positions: Iterable<Position>
) {
    for (pos1 in positions) {
        val rowIndex2 = length - pos1.rowIndex - 1
        val pos2 = Position(rowIndex2, pos1.colIndex)

        swapGraphValues(graph, pos1, pos2)
        swapBoardValues(board, pos1, pos2)
    }
}

private fun horizontalFlip(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    length: Int,
    positions: Iterable<Position>
) {
    for (pos1 in positions) {
        val colIndex2 = length - pos1.colIndex - 1
        val pos2 = Position(pos1.rowIndex, colIndex2)

        swapGraphValues(graph, pos1, pos2)
        swapBoardValues(board, pos1, pos2)
    }
}

private fun transposeShuffle(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    positions: Iterable<Position>,
    rand: Random
) {
    if (rand.nextBoolean()) {
        doTranspose(graph, board, positions)
    }
}

private fun doTranspose(graph: SudokuGraph, board: List<MutableList<Int>>, positions: Iterable<Position>) {
    for ((rowIndex, colIndex) in positions) {
        val pos1 = Position(rowIndex, colIndex)
        val pos2 = Position(colIndex, rowIndex)

        swapGraphValues(graph, pos1, pos2)
        swapBoardValues(board, pos1, pos2)
    }
}

private fun rotateShuffle(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    length: Int,
    positions: Iterable<Position>,
    rand: Random
) {
    val amountOfRotations = rand.nextInt(4)

    repeat(amountOfRotations) { _ ->
        rotate90(graph, board, length, positions)
    }
}

private fun rotate90(graph: SudokuGraph, board: List<MutableList<Int>>, length: Int, positions: Iterable<Position>) {
    horizontalFlip(graph, board, length, positions)
    doTranspose(graph, board, positions)
}

private fun swapShuffle(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    length: Int,
    positions: Iterable<Position>,
    rand: Random
) {
    val shuffledMapping = createShuffledMapping(length, rand)
    val associations = groupCellsByValue(board, length, positions)

    for ((value, newValue) in shuffledMapping) {
        val associatedPositions = associations.getValue(value)

        for (pos in associatedPositions) {
            if (null !== graph.at(pos).value) {
                graph.setAt(pos, newValue)
            }

            board[pos.rowIndex][pos.colIndex] = newValue
        }
    }
}

private fun createShuffledMapping(length: Int, rand: Random): Map<Int, Int> {
    val values = MutableList(length) { it + 1 }
    val map = HashMap<Int, Int>(length)

    for (value in 1 .. length) {
        val index = rand.nextInt(values.size)
        val randomValue = values.removeAt(index)

        if (value != randomValue) {
            map[value] = randomValue
        }
    }

    return map
}

private fun groupCellsByValue(
    board: List<List<Int>>,
    length: Int,
    positions: Iterable<Position>
): Map<Int, List<Position>> {
    val map = initializeSwapMap(length)

    for (pos in positions) {
        val value = board[pos.rowIndex][pos.colIndex]

        map.getValue(value).add(pos)
    }

    return map
}

private fun initializeSwapMap(length: Int): HashMap<Int, MutableList<Position>> {
    val map = HashMap<Int, MutableList<Position>>(length)

    for (value in 1 .. length) {
        map[value] = ArrayList(length)
    }

    return map
}

private fun boxShuffle(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    range: IntRange,
    dimension: Dimension,
    rand: Random
) {
    swapBoxesByRow(graph, board, range, dimension, rand)
    swapBoxesByCol(graph, board, range, dimension, rand)
}

private fun swapBoxesByRow(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    range: IntRange,
    dimension: Dimension,
    rand: Random
) {
    @Suppress("UnnecessaryVariable", "RedundantSuppression")
    val colRange = range

    val length = dimension.length
    val rowSize = dimension.boxRows

    for (startRowIndex1 in range step rowSize) {
        val endRowIndex1 = startRowIndex1 + rowSize
        val rowRange1 = startRowIndex1 until endRowIndex1

        for (startRowIndex2 in endRowIndex1 until length step rowSize) {
            if (rand.nextBoolean()) {
                continue
            }

            val endRowIndex2 = startRowIndex2 + rowSize
            val rowRange2 = startRowIndex2 until endRowIndex2

            swapBoxesByRowHelper(graph, board, rowRange1, rowRange2, colRange)
        }
    }
}

private fun swapBoxesByRowHelper(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    rowRange1: IntRange,
    rowRange2: IntRange,
    colRange: IntRange
) {
    for ((rowIndex1, rowIndex2) in rowRange1.thru(rowRange2)) {
        for (colIndex in colRange) {
            val pos1 = Position(rowIndex1, colIndex)
            val pos2 = Position(rowIndex2, colIndex)

            swapGraphValues(graph, pos1, pos2)
            swapBoardValues(board, pos1, pos2)
        }
    }
}

private fun swapBoxesByCol(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    range: IntRange,
    dimension: Dimension,
    rand: Random
) {
    @Suppress("UnnecessaryVariable", "RedundantSuppression")
    val rowRange = range

    val length = dimension.length
    val colSize = dimension.boxCols

    for (startColIndex1 in range step colSize) {
        val endColIndex1 = startColIndex1 + colSize
        val colRange1 = startColIndex1 until endColIndex1

        for (startColIndex2 in endColIndex1 until length step colSize) {
            if (rand.nextBoolean()) {
                continue
            }

            val endColIndex2 = startColIndex2 + colSize
            val colRange2 = startColIndex2 until endColIndex2

            swapBoxesByColHelper(graph, board, rowRange, colRange1, colRange2)
        }
    }
}

private fun swapBoxesByColHelper(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    rowRange: IntRange,
    colRange1: IntRange,
    colRange2: IntRange
) {
    for ((colIndex1, colIndex2) in colRange1.thru(colRange2)) {
        for (rowIndex in rowRange) {
            val pos1 = Position(rowIndex, colIndex1)
            val pos2 = Position(rowIndex, colIndex2)

            swapGraphValues(graph, pos1, pos2)
            swapBoardValues(board, pos1, pos2)
        }
    }
}

private fun innerBoxShuffle(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    range: IntRange,
    dimension: Dimension,
    rand: Random
) {
    swapInnerBoxesByRow(graph, board, range, dimension.boxRows, rand)
    swapInnerBoxesByCol(graph, board, range, dimension.boxCols, rand)
}

private fun swapInnerBoxesByRow(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    range: IntRange,
    rowSize: Int,
    rand: Random
) {
    @Suppress("UnnecessaryVariable", "RedundantSuppression")
    val colRange = range

    for (rowIndex1 in range step rowSize) {
        val rowRange = (rowIndex1 + 1) up (rowSize - 1)

        for (rowIndex2 in rowRange) {
            if (rand.nextBoolean()) {
                continue
            }

            swapInnerBoxesByRowHelper(graph, board, rowIndex1, rowIndex2, colRange)
        }
    }
}

private fun swapInnerBoxesByRowHelper(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    rowIndex1: Int,
    rowIndex2: Int,
    colRange: IntRange
) {
    for (colIndex in colRange) {
        val pos1 = Position(rowIndex1, colIndex)
        val pos2 = Position(rowIndex2, colIndex)

        swapGraphValues(graph, pos1, pos2)
        swapBoardValues(board, pos1, pos2)
    }
}

private fun swapInnerBoxesByCol(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    range: IntRange,
    colSize: Int,
    rand: Random
) {
    @Suppress("UnnecessaryVariable", "RedundantSuppression")
    val rowRange = range

    for (colIndex1 in range step colSize) {
        val colRange = (colIndex1 + 1) up (colSize - 1)

        for (colIndex2 in colRange) {
            if (rand.nextBoolean()) {
                continue
            }

            swapInnerBoxesByColHelper(graph, board, rowRange, colIndex1, colIndex2)
        }
    }
}

private fun swapInnerBoxesByColHelper(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    rowRange: IntRange,
    colIndex1: Int,
    colIndex2: Int
) {
    for (rowIndex in rowRange) {
        val pos1 = Position(rowIndex, colIndex1)
        val pos2 = Position(rowIndex, colIndex2)

        swapGraphValues(graph, pos1, pos2)
        swapBoardValues(board, pos1, pos2)
    }
}

private fun swapGraphValues(graph: SudokuGraph, pos1: Position, pos2: Position) {
    val node1 = graph.at(pos1)
    val node2 = graph.at(pos2)

    val temp = node1.value
    node1.value = node2.value
    node2.value = temp
}

private fun swapBoardValues(board: List<MutableList<Int>>, pos1: Position, pos2: Position) {
    val row1 = board[pos1.rowIndex]
    val row2 = board[pos2.rowIndex]

    val temp = row1[pos1.colIndex]
    row1[pos1.colIndex] = row2[pos2.colIndex]
    row2[pos2.colIndex] = temp
}
