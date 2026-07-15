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
        if (checkLowerBound(node, lowerBound)) {
            givenCount = attemptToRemoveValueFromNode(graph, node, length, givenCount)

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
    var result = tooManyNonnulls(node.row, lowerBound)
    result = result && tooManyNonnulls(node.column, lowerBound)
    result = result && tooManyNonnulls(node.box, lowerBound)

    if (node.hyper.isNotEmpty()) {
        result = result && tooManyNonnulls(node.hyper, lowerBound)
    }

    return result
}

private fun tooManyNonnulls(nodeSet: Set<SudokuNode>, lowerBound: Int): Boolean {
    val nonnullCount = nodeSet.count { null !== it.value }

    return nonnullCount >= lowerBound
}

private fun attemptToRemoveValueFromNode(
    graph: SudokuGraph,
    node: SudokuNode,
    length: Int,
    currentGivenCount: Int
): Int {
    var newGivenCount = currentGivenCount

    if (removeValueIfOneSolution(graph, length, node)) {
        --newGivenCount
    }

    return newGivenCount
}

private fun removeValueIfOneSolution(graph: SudokuGraph, length: Int, node: SudokuNode): Boolean {
    val temp = node.value!! // null check intended to ensure "temp" value is never null

    node.value = null

    if (hasOneSolution(graph, length)) {
        return true
    }

    node.value = temp

    return false
}
