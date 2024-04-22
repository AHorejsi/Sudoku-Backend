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

    val targetGivenCount = determineAmountOfGivens(difficulty, length, rand)
    val lowerBound = determineLowerBound(difficulty, length)

    adjustForDifficultyHelper1(
        neighborhoods,
        targetGivenCount,
        lowerBound,
        rand,
        info.dimension
    )
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
    targetGivenCount: Int,
    lowerBound: Int,
    rand: Random,
    dimension: Dimension
) {
    val length = dimension.length

    var givenCount = length * length

    for (node in neighborhoods.asSequence().shuffled(rand)) {
        val topLeft = node.place
        val bottomRight = Position(length - topLeft.rowIndex - 1, length - topLeft.colIndex - 1)
        val rand1 = Position(rand.nextInt(length), rand.nextInt(length))
        val rand2 = Position(rand.nextInt(length), rand.nextInt(length))

        val positions = listOf(topLeft, bottomRight, rand1, rand2).asSequence().shuffled(rand).iterator()

        givenCount -= adjustForDifficultyHelper2(positions, lowerBound, neighborhoods, length, dimension)

        if (givenCount < targetGivenCount) {
            break
        }
    }
}

private fun adjustForDifficultyHelper2(
    positions: Iterator<Position>,
    lowerBound: Int,
    neighborhoods: List<SudokuNode>,
    length: Int,
    dimension: Dimension
): Int {
    var amountRemoved = 0

    while (positions.hasNext()) {
        val pos = positions.next()

        if (checkLowerBound(pos, lowerBound, neighborhoods, dimension)) {
            val node = neighborhoods.get2d(pos.rowIndex, pos.colIndex, length)

            if (null !== node.value && tryRemove(neighborhoods, length, node)) {
                ++amountRemoved
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
        val node = neighborhoods.get2d(rowIndex, colIndex, length)

        if (null !== node.value) {
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
        val node = neighborhoods.get2d(rowIndex, colIndex, length)

        if (null !== node.value) {
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
            val node = neighborhoods.get2d(rowIndex, colIndex, length)

            if (null !== node.value) {
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
): Boolean {
    val temp = node.value

    node.value = null

    if (hasUniqueSolution(neighborhoods, length)) {
        return true
    }

    node.value = temp

    return false
}
