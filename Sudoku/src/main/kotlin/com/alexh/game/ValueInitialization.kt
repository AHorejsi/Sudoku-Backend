package com.alexh.game

import com.alexh.utils.up
import kotlin.random.Random

internal fun initializeValues(graph: SudokuGraph, dimension: Dimension, games: Set<Game>, rand: Random) {
    val length = dimension.length
    val legal = 1 .. length

    initializeValuesHelper1(graph, dimension, rand, legal, games)

    val unassigned = retrieveNodesWithNullValues(graph)
    val legalMap = shuffleValues(graph, length, rand, legal)

    initializeValuesHelper2(unassigned, legalMap)
}

private fun initializeValuesHelper1(
    graph: SudokuGraph,
    dimension: Dimension,
    rand: Random,
    legal: IntRange,
    games: Set<Game>
) {
    if (Game.HYPER in games) {
        val hyperRows = 1 up dimension.boxRows
        val hyperCols = 1 up dimension.boxCols

        fillBox(graph, hyperRows, hyperCols, legal, rand)
    }
    else {
        fillRegularDiagonal(graph, dimension, rand, legal)
    }
}

private fun fillRegularDiagonal(
    graph: SudokuGraph,
    dimension: Dimension,
    rand: Random,
    legal: IntRange
) {
    val length = dimension.length
    val boxRows = dimension.boxRows
    val boxCols = dimension.boxCols

    val rowRange = 0 until length step boxRows
    val colRange = 0 until length step boxCols

    for ((startRowIndex, startColIndex) in rowRange.zip(colRange)) {
        val rows = startRowIndex up boxRows
        val cols = startColIndex up boxCols

        fillBox(graph, rows, cols, legal, rand)
    }
}

private fun fillBox(
    graph: SudokuGraph,
    rows: IntRange,
    cols: IntRange,
    legal: IntRange,
    rand: Random
) {
    val shuffledValues = shuffleLegalValues(legal, rand).iterator()

    for (rowIndex in rows) {
        for (colIndex in cols) {
            val value = shuffledValues.next()

            graph.set(rowIndex, colIndex, value)
        }
    }
}

private fun retrieveNodesWithNullValues(graph: SudokuGraph): MutableList<SudokuNode> {
    val unassigned = ArrayList<SudokuNode>(graph.size)

    for (node in graph) {
        if (null === node.value) {
            unassigned.add(node)
        }
    }

    return unassigned
}

private fun shuffleValues(
    graph: SudokuGraph,
    length: Int,
    rand: Random,
    legal: IntRange
): Map<SudokuNode, List<Int>> {
    val range = 0 until length
    val legalMap = HashMap<SudokuNode, List<Int>>(length * length)

    for (rowIndex in range) {
        for (colIndex in range) {
            val node = graph.get(rowIndex, colIndex)

            if (null === node.value) {
                legalMap[node] = shuffleLegalValues(legal, rand)
            }
        }
    }

    return legalMap
}

private fun shuffleLegalValues(legal: IntRange, rand: Random): List<Int> {
    val list = legal.toMutableList()
    list.shuffle(rand)

    return list
}

private fun initializeValuesHelper2(
    unassigned: MutableList<SudokuNode>,
    legalMap: Map<SudokuNode, List<Int>>
): Boolean {
    if (unassigned.isEmpty()) {
        return true
    }

    val node = unassigned.removeLast()
    val valueList = legalMap.getValue(node)

    for (value in valueList) {
        if (isSafe(value, node)) {
            node.value = value

            if (initializeValuesHelper2(unassigned, legalMap)) {
                return true
            }

            node.value = null
        }
    }

    unassigned.add(node)

    return false
}

private fun isSafe(value: Int, node: SudokuNode): Boolean {
    for (neighbor in node.all) {
        if (value == neighbor.value) {
            return false
        }
    }

    return true
}
