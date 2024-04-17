package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.get2d
import com.alexh.utils.up
import kotlin.random.Random

internal fun adjustForDifficulty(
    neighborhoods: List<SudokuNode>,
    info: MakeSudokuCommand
) {
    val difficulty = info.difficulty
    val rand = info.random
    val length = info.dimension.length

    val amountOfGivens = determineAmountOfGivens(difficulty, length, rand)
    val lowerBound = determineLowerBound(difficulty, length)

    adjustForDifficultyHelper1(neighborhoods, amountOfGivens, lowerBound, rand, info.dimension)
}

private fun determineAmountOfGivens(
    difficulty: Difficulty,
    length: Int,
    rand: Random
): Int {
    val count = length * length
    val minCount = (count * difficulty.lowerBoundOfInitialGivens).toInt()
    val maxCount = (count * difficulty.upperBoundOfInitialGivens).toInt()

    return rand.nextInt(minCount, maxCount)
}

private fun determineLowerBound(
    difficulty: Difficulty,
    length: Int
): Int = (length * difficulty.lowerBoundOfInitialGivensPerNeighborhood).toInt()

private fun adjustForDifficultyHelper1(
    neighborhoods: List<SudokuNode>,
    amountOfGivens: Int,
    lowerBound: Int,
    rand: Random,
    dimension: Dimension
) {
    val length = dimension.length

    var count = length * length

    for (topLeft in neighborhoods.asSequence().map{ it.place }.shuffled(rand)) {
        val bottomRight = Position(length - topLeft.rowIndex - 1, length - topLeft.colIndex - 1)
        val topRight = Position(topLeft.rowIndex, bottomRight.colIndex)
        val bottomLeft = Position(bottomRight.rowIndex, topLeft.colIndex)

        val positions = listOf(topLeft, bottomRight, topRight, bottomLeft).shuffled(rand)

        count -= adjustforDifficultyHelper2(positions, lowerBound, neighborhoods, length, dimension)

        if (count <= amountOfGivens) {
            return
        }
    }
}

private fun adjustforDifficultyHelper2(
    positions: List<Position>,
    lowerBound: Int,
    neighborhoods: List<SudokuNode>,
    length: Int,
    dimension: Dimension
): Int {
    var amountRemoved = 0

    for (pos in positions) {
        if (checkLowerBound(pos, lowerBound, neighborhoods, dimension)) {
            val node = neighborhoods.get2d(pos.rowIndex, pos.colIndex, length)

            if (null !== node.value) {
                amountRemoved += tryRemove(neighborhoods, length, node)
            }
        }
    }

    return amountRemoved
}

private fun checkLowerBound(
    pos: Position,
    lowerBound: Int,
    neighborhoods: List<SudokuNode>,
    dimension: Dimension
): Boolean {
    val length = dimension.length
    val range = 0 until length

    val rows = checkRow(pos, lowerBound, neighborhoods, range, length)
    val cols = checkCol(pos, lowerBound, neighborhoods, range, length)
    val boxes = checkBox(pos, lowerBound, neighborhoods, length, dimension)

    return rows && cols && boxes
}


private fun checkRow(
    pos: Position,
    lowerBound: Int,
    neighborhoods: List<SudokuNode>,
    range: IntRange,
    length: Int
): Boolean {
    var count = 0

    val rowIndex = pos.rowIndex

    for (colIndex in range) {
        if (null !== neighborhoods.get2d(rowIndex, colIndex, length).value) {
            ++count
        }
    }

    return count >= lowerBound
}

private fun checkCol(
    pos: Position,
    lowerBound: Int,
    neighborhoods: List<SudokuNode>,
    range: IntRange,
    length: Int
): Boolean {
    var count = 0

    val colIndex = pos.colIndex

    for (rowIndex in range) {
        if (null !== neighborhoods.get2d(rowIndex, colIndex, length).value) {
            ++count
        }
    }

    return count >= lowerBound
}

private fun checkBox(
    pos: Position,
    lowerBound: Int,
    neighborhoods: List<SudokuNode>,
    length: Int,
    dimension: Dimension
): Boolean {
    var count = 0

    val rowRange = pos.rowIndex - pos.rowIndex % dimension.boxRows up dimension.boxRows
    val colRange = pos.colIndex - pos.colIndex % dimension.boxCols up dimension.boxCols

    for (rowIndex in rowRange) {
        for (colIndex in colRange) {
            if (null !== neighborhoods.get2d(rowIndex, colIndex, length).value) {
                ++count
            }
        }
    }

    return count >= lowerBound
}

private fun tryRemove(
    neighborhoods: List<SudokuNode>,
    length: Int,
    node: SudokuNode
): Int {
    val value = node.value

    node.value = null

    if (!hasUniqueSolution(neighborhoods, length)) {
        node.value = value

        return 0
    }

    return 1
}
