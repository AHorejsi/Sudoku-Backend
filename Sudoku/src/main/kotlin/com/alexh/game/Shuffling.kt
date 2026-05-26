package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.up
import kotlin.random.Random

internal fun shuffleBoard(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    dimension: Dimension,
    games: Set<Game>,
    rand: Random
) {
    flipShuffle(graph, board, dimension, rand)
    transposeShuffle(graph, board, dimension, rand)
    rotateShuffle(graph, board, dimension, rand)
    swapShuffle(graph, board, dimension, rand)

    if (Game.HYPER !in games) {
        boxShuffle(graph, board, dimension, rand)
        innerBoxShuffle(graph, board, dimension, rand)
    }
}

private fun flipShuffle(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    dimension: Dimension,
    rand: Random
) {
    val length = dimension.length
    val range = 0 until length

    if (rand.nextBoolean()) {
        verticalFlip(graph, board, length, range)
    }

    if (rand.nextBoolean()) {
        horizontalFlip(graph, board, length, range)
    }
}

private fun verticalFlip(graph: SudokuGraph, board: List<MutableList<Int>>, length: Int, range: IntRange) {
    for (rowIndex1 in range) {
        for (colIndex in range) {
            val rowIndex2 = length - rowIndex1 - 1

            val pos1 = Position(rowIndex1, colIndex)
            val pos2 = Position(rowIndex2, colIndex)

            swapGraphValues(graph, pos1, pos2)
            swapBoardValues(board, pos1, pos2)
        }
    }
}

private fun horizontalFlip(graph: SudokuGraph, board: List<MutableList<Int>>, length: Int, range: IntRange) {
    for (colIndex1 in range) {
        for (rowIndex in range) {
            val colIndex2 = length - colIndex1 - 1

            val pos1 = Position(rowIndex, colIndex1)
            val pos2 = Position(rowIndex, colIndex2)

            swapGraphValues(graph, pos1, pos2)
            swapBoardValues(board, pos1, pos2)
        }
    }
}

private fun transposeShuffle(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    dimension: Dimension,
    rand: Random
) {
    if (rand.nextBoolean()) {
        doTranspose(graph, board, dimension.length)
    }
}

private fun doTranspose(graph: SudokuGraph, board: List<MutableList<Int>>, length: Int) {
    val range = 0 until length

    for (rowIndex in range) {
        for (colIndex in range) {
            val pos1 = Position(rowIndex, colIndex)
            val pos2 = Position(colIndex, rowIndex)

            swapGraphValues(graph, pos1, pos2)
            swapBoardValues(board, pos1, pos2)
        }
    }
}

private fun rotateShuffle(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    dimension: Dimension,
    rand: Random
) {
    val amountOfRotations = rand.nextInt(4)
    val length = dimension.length

    repeat(amountOfRotations) { _ ->
        rotate90(graph, board, length)
    }
}

private fun rotate90(graph: SudokuGraph, board: List<MutableList<Int>>, length: Int) {
    horizontalFlip(graph, board, length, 0 until length)
    doTranspose(graph, board, length)
}

private fun swapShuffle(
    graph: SudokuGraph,
    board: List<MutableList<Int>>,
    dimension: Dimension,
    rand: Random
) {
    val shuffledMapping = createShuffledMapping(dimension.length, rand)
    val associations = groupCellsByValue(board, dimension.length)

    for ((value, newValue) in shuffledMapping) {
        val positions = associations.getValue(value)

        for (pos in positions) {
            if (null !== graph.get(pos).value) {
                graph.set(pos, newValue)
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

private fun groupCellsByValue(board: List<List<Int>>, length: Int): Map<Int, List<Position>> {
    val map = initializeSwapMap(length)
    val range = 0 until length

    for (rowIndex in range) {
        for (colIndex in range) {
            val value = board[rowIndex][colIndex]
            val pos = Position(rowIndex, colIndex)

            map.getValue(value).add(pos)
        }
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
    dimension: Dimension,
    rand: Random
) {
    swapBoxesByRow(graph, board, dimension, rand)
    swapBoxesByCol(graph, board, dimension, rand)
}

private fun swapBoxesByRow(graph: SudokuGraph, board: List<MutableList<Int>>, dimension: Dimension, rand: Random) {
    val length = dimension.length
    val rowSize = dimension.boxRows
    val colRange = 0 until length

    for (startRowIndex1 in 0 until length step rowSize) {
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
    for ((rowIndex1, rowIndex2) in rowRange1.zip(rowRange2)) {
        for (colIndex in colRange) {
            val pos1 = Position(rowIndex1, colIndex)
            val pos2 = Position(rowIndex2, colIndex)

            swapGraphValues(graph, pos1, pos2)
            swapBoardValues(board, pos1, pos2)
        }
    }
}

private fun swapBoxesByCol(graph: SudokuGraph, board: List<MutableList<Int>>, dimension: Dimension, rand: Random) {
    val length = dimension.length
    val colSize = dimension.boxCols
    val rowRange = 0 until length

    for (startColIndex1 in 0 until length step colSize) {
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
    for ((colIndex1, colIndex2) in colRange1.zip(colRange2)) {
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
    dimension: Dimension,
    rand: Random
) {
    swapInnerBoxesByRow(graph, board, dimension, rand)
    swapInnerBoxesByCol(graph, board, dimension, rand)
}

private fun swapInnerBoxesByRow(graph: SudokuGraph, board: List<MutableList<Int>>, dimension: Dimension, rand: Random) {
    val length = dimension.length
    val rowSize = dimension.boxRows
    val colRange = 0 until length

    for (rowIndex1 in 0 until length step rowSize) {
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

private fun swapInnerBoxesByCol(graph: SudokuGraph, board: List<MutableList<Int>>, dimension: Dimension, rand: Random) {
    val length = dimension.length
    val colSize = dimension.boxCols
    val rowRange = 0 until length

    for (colIndex1 in 0 until length step colSize) {
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
    val node1 = graph.get(pos1)
    val node2 = graph.get(pos2)

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
