package com.alexh.game

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
        length
    )
}

private fun determineAmountOfGivens(
    difficulty: Difficulty,
    length: Int,
    rand: Random
): Int {
    val givenCount = length * length

    val minCount = (givenCount * difficulty.initialGivenLowerBound).toInt()
    val maxCount = (givenCount * difficulty.initialGivenUpperBound).toInt()

    return rand.nextInt(minCount, maxCount + 1)
}

private fun determineLowerBound(
    difficulty: Difficulty,
    length: Int
): Int = (length * difficulty.initialGivensPerNeighborhood).toInt()

private fun adjustForDifficultyHelper1(
    neighborhoods: List<SudokuNode>,
    targetGivenCount: Int,
    lowerBound: Int,
    rand: Random,
    length: Int
) {
    var givenCount = length * length

    for (node in neighborhoods.asSequence().shuffled(rand)) {
        if (adjustForDifficultyHelper2(node, lowerBound, neighborhoods, length)) {
            --givenCount

            if (givenCount == targetGivenCount) {
                return
            }
        }
    }
}

private fun adjustForDifficultyHelper2(
    node: SudokuNode,
    lowerBound: Int,
    neighborhoods: List<SudokuNode>,
    length: Int
): Boolean {
    if (checkLowerBound(node, lowerBound)) {
        if (tryRemove(neighborhoods, length, node)) {
            return true
        }
    }

    return false
}

private fun checkLowerBound(
    node: SudokuNode,
    lowerBound: Int
): Boolean {
    var count = 0

    for (neighbor in node.row) {
        if (null !== neighbor.value) {
            ++count

            if (count == lowerBound) {
                return false
            }
        }
    }

    count = 0

    for (neighbor in node.col) {
        if (null !== neighbor.value) {
            ++count

            if (count == lowerBound) {
                return false
            }
        }
    }

    count = 0

    for (neighbor in node.box) {
        if (null !== neighbor.value) {
            ++count

            if (count == lowerBound) {
                return false
            }
        }
    }

    count = 0

    for (neighbor in node.hyper) {
        if (null !== neighbor.value) {
            ++count

            if (count == lowerBound) {
                return false
            }
        }
    }

    return true
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
