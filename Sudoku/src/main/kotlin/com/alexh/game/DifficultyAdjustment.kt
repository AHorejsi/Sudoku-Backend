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

    val givenCount = length * length
    val targetGivenCount = determineAmountOfGivens(difficulty, givenCount, rand)
    val lowerBound = determineLowerBound(difficulty, length)

    adjustForDifficultyHelper1(
        neighborhoods,
        givenCount,
        targetGivenCount,
        lowerBound,
        rand,
        info.dimension
    )
}

private fun determineAmountOfGivens(
    difficulty: Difficulty,
    givenCount: Int,
    rand: Random
): Int {
    val minCount = (givenCount * difficulty.lowerBoundOfInitialGivens).toInt()
    val maxCount = (givenCount * difficulty.upperBoundOfInitialGivens).toInt()

    return rand.nextInt(minCount, maxCount + 1)
}

private fun determineLowerBound(
    difficulty: Difficulty,
    length: Int
): Int = (length * difficulty.lowerBoundOfInitialGivensPerNeighborhood).toInt()

private fun adjustForDifficultyHelper1(
    neighborhoods: List<SudokuNode>,
    givenCount: Int,
    targetGivenCount: Int,
    lowerBound: Int,
    rand: Random,
    dimension: Dimension
) {
    var newGivenCount = givenCount

    for (node in neighborhoods.asSequence().shuffled(rand)) {
        if (adjustForDifficultyHelper2(node, lowerBound, neighborhoods, dimension)) {
            --newGivenCount

            if (newGivenCount <= targetGivenCount) {
                break
            }
        }
    }
}

private fun adjustForDifficultyHelper2(
    node: SudokuNode,
    lowerBound: Int,
    neighborhoods: List<SudokuNode>,
    dimension: Dimension
): Boolean {
    if (checkLowerBound(node.place, lowerBound, neighborhoods, dimension)) {
        if (tryRemove(neighborhoods, dimension.length, node)) {
            return true
        }
    }

    return false
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
    val temp = node.value!!

    node.value = null

    if (hasUniqueSolution(neighborhoods, length)) {
        return true
    }

    node.value = temp

    return false
}
