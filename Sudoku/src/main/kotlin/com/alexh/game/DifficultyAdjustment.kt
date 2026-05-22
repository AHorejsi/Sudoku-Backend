package com.alexh.game

import kotlin.random.Random

internal fun adjustForDifficulty(graph: SudokuGraph, info: MakeSudokuCommand) {
    val difficulty = info.difficulty
    val rand = info.random
    val length = info.dimension.length

    val targetGivenCount = determineAmountOfGivens(difficulty, length, rand)
    val lowerBound = (length * difficulty.initialGivensPerNeighborhood).toInt()

    var givenCount = length * length

    for (node in graph.shuffled(rand)) {
        if (checkLowerBound(node, lowerBound) && tryRemove(graph, length, node)) {
            --givenCount

            if (givenCount <= targetGivenCount) {
                break
            }
        }
    }
}

private fun determineAmountOfGivens(difficulty: Difficulty, length: Int, rand: Random): Int {
    val givenCount = length * length

    val minCount = (givenCount * difficulty.initialGivenLowerBound).toInt()
    val maxCount = (givenCount * difficulty.initialGivenUpperBound).toInt() + 1

    return rand.nextInt(minCount, maxCount)
}

private fun checkLowerBound(node: SudokuNode, lowerBound: Int): Boolean {
    var result = node.row.count{ null !== it.value } >= lowerBound
    result = result && node.column.count{ null !== it.value } >= lowerBound
    result = result && node.box.count{ null !== it.value } >= lowerBound

    if (node.hyper.isNotEmpty()) {
        result = result && node.hyper.count{ null !== it.value } >= lowerBound
    }

    return result
}

private fun tryRemove(graph: SudokuGraph, length: Int, node: SudokuNode): Boolean {
    val temp = node.value!!

    node.value = null

    if (hasUniqueSolution(graph, length)) {
        return true
    }

    node.value = temp

    return false
}
