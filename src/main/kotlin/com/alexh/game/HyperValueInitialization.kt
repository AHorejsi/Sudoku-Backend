package com.alexh.game

import com.alexh.utils.Position
import kotlin.random.Random

fun initialValuesForHyper(puzzle: HyperSudoku, rand: Random) {
    val legal = puzzle.legal.toMutableList()

    initializeValuesHelper1(puzzle, legal, rand)

    val valueMap = shuffleValues(legal, puzzle, rand)
    val initial = Position(0, 0)

    println(initializeValuesHelper2(puzzle, valueMap, initial))
}

private fun initializeValuesHelper1(puzzle: HyperSudoku, legal: MutableList<Int>, rand: Random) {
    val length = puzzle.length
    val boxRows = puzzle.boxRows
    val boxCols = puzzle.boxCols
    var startRowIndex = 1
    var startColIndex = 1

    while (startRowIndex < length && startColIndex < length) {
        val endRowIndex = startRowIndex + boxRows
        val endColIndex = startColIndex + boxCols

        assignValuesToBox(puzzle, legal, startRowIndex, startColIndex, endRowIndex, endColIndex, rand)

        startRowIndex += boxRows + 1
        startColIndex += boxCols + 1
    }
}

private fun assignValuesToBox(
    puzzle: HyperSudoku,
    legal: MutableList<Int>,
    startRowIndex: Int,
    startColIndex: Int,
    endRowIndex: Int,
    endColIndex: Int,
    rand: Random
) {
    legal.shuffle(rand)

    var legalIndex = 0

    for (rowIndex in startRowIndex until endRowIndex) {
        for (colIndex in startColIndex until endColIndex) {
            puzzle.setValue(rowIndex, colIndex, legal[legalIndex])

            ++legalIndex
        }
    }
}

private fun shuffleValues(legal: List<Int>, puzzle: HyperSudoku, rand: Random): Map<Position, List<Int>> {
    val length = legal.size
    val valueMap = mutableMapOf<Position, List<Int>>()

    for (rowIndex in 0 until length) {
        for (colIndex in 0 until length) {
            if (null === puzzle.getValue(rowIndex, colIndex)) {
                val copy = legal.shuffled(rand)
                val pos = Position(rowIndex, colIndex)

                valueMap[pos] = copy
            }
        }
    }

    return valueMap
}

private fun initializeValuesHelper2(puzzle: HyperSudoku, valueMap: Map<Position, List<Int>>, prev: Position): Boolean {
    val next = nextPosition(prev, puzzle)

    if (next.rowIndex == puzzle.length) {
        return true
    }

    val legal = valueMap.getValue(next)
    val rowIndex = next.rowIndex
    val colIndex = next.colIndex

    for (value in legal) {
        if (puzzle.isSafe(rowIndex, colIndex, value)) {

            puzzle.setValue(rowIndex, colIndex, value)

            if (initializeValuesHelper2(puzzle, valueMap, next)) {
                return true
            }

            puzzle.deleteValue(rowIndex, colIndex)
        }
    }

    return false
}

private fun nextPosition(prev: Position, puzzle: HyperSudoku): Position {
    val length = puzzle.length
    var rowIndex = prev.rowIndex
    var colIndex = prev.colIndex


    while (null !== puzzle.getValue(rowIndex, colIndex)) {
        ++colIndex

        if (colIndex == puzzle.length) {
            ++rowIndex
            colIndex = 0

            if (rowIndex == length) {
                break
            }
        }
    }

    return Position(rowIndex, colIndex)
}
