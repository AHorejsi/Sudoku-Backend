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
    val lowerBound = (length * difficulty.initialGivensPerNeighborhood).toInt()

    var givenCount = length * length

    for (node in neighborhoods.asSequence().shuffled(rand)) {
        if (checkLowerBound(node, lowerBound) && tryRemove(neighborhoods, length, node)) {
            --givenCount

            if (givenCount <= targetGivenCount) {
                break
            }
        }
    }
}

private fun determineAmountOfGivens(
    difficulty: Difficulty,
    length: Int,
    rand: Random
): Int {
    val givenCount = length * length

    val minCount = (givenCount * difficulty.initialGivenLowerBound).toInt()
    val maxCount = (givenCount * difficulty.initialGivenUpperBound).toInt() + 1

    return rand.nextInt(minCount, maxCount)
}

private fun checkLowerBound(
    node: SudokuNode,
    lowerBound: Int
): Boolean {
    val rows = node.row.count{ null !== it.value } >= lowerBound
    val cols = node.column.count{ null !== it.value } >= lowerBound
    val boxes = node.box.count{ null !== it.value } >= lowerBound

    var result = rows && cols && boxes

    if (node.hyper.any()) {
        result = result && node.hyper.count{ null !== it.value } >= lowerBound
    }

    return result
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
