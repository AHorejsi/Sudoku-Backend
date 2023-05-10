package com.alexh.game

import kotlin.math.round

internal fun adjustForDifficultyForRegular(puzzle: RegularSudoku) {
    val amountOfGivens = decideAmountOfGivens(puzzle)
    val lowerBoundOfGivensPerUnit = decideLowerBoundOfGivensPerUnit(puzzle)

    doAdjustment(puzzle, amountOfGivens, lowerBoundOfGivensPerUnit)
}

private fun decideAmountOfGivens(puzzle: RegularSudoku): Int {
    val total = puzzle.length * puzzle.length
    val lowerBound = puzzle.lowerBoundOfInitialGivens
    val upperBound = puzzle.upperBoundOfInitialGivens
    val percent = (lowerBound..upperBound).random()

    return round(total * (percent / 100.0)).toInt()
}

private fun decideLowerBoundOfGivensPerUnit(puzzle: RegularSudoku): Int =
    round(puzzle.length * (puzzle.lowerBoundOfInitialGivensPerUnit / 100.0)).toInt()

private fun doAdjustment(puzzle: RegularSudoku, amountOfGivens: Int, lowerBoundOfGivensPerUnit: Int) {
    val length = puzzle.length
    var valueCount = length * length
    val range = 0 until length

    for (rowIndex1 in range) {
        for (colIndex1 in range) {
            if (checkLowerBound(puzzle, rowIndex1, colIndex1, lowerBoundOfGivensPerUnit)) {
                valueCount = tryRemove(puzzle, rowIndex1, colIndex1, valueCount)

                if (valueCount == amountOfGivens) {
                    return
                }
            }

            val rowIndex2 = length - rowIndex1 - 1
            val colIndex2 = length - colIndex1 - 1

            if (checkLowerBound(puzzle, rowIndex2, colIndex2, lowerBoundOfGivensPerUnit)) {
                valueCount = tryRemove(puzzle, rowIndex2, colIndex2, valueCount)

                if (valueCount == amountOfGivens) {
                    return
                }
            }

            val rowIndex3 = range.random()
            val colIndex3 = range.random()

            if (checkLowerBound(puzzle, rowIndex3, colIndex3, lowerBoundOfGivensPerUnit)) {
                valueCount = tryRemove(puzzle, rowIndex3, colIndex3, valueCount)

                if (valueCount == amountOfGivens) {
                    return
                }
            }
        }
    }
}

private fun checkLowerBound(puzzle: RegularSudoku, rowIndex: Int, colIndex: Int, lowerBoundOfGivensPerUnit: Int): Boolean {
    val (rowGivenCount, colGivenCount, boxGivenCount) = puzzle.givens(rowIndex, colIndex)

    val rowResult = rowGivenCount >= lowerBoundOfGivensPerUnit
    val colResult = colGivenCount >= lowerBoundOfGivensPerUnit
    val boxResult = boxGivenCount >= lowerBoundOfGivensPerUnit

    return rowResult && colResult && boxResult
}

private fun tryRemove(puzzle: RegularSudoku, rowIndex: Int, colIndex: Int, valueCount: Int): Int {
    val value = puzzle.getValue(rowIndex, colIndex)

    puzzle.deleteValue(rowIndex, colIndex)

    if (hasUniqueSolution(puzzle)) {
        return valueCount - 1
    }
    else {
        puzzle.setValue(rowIndex, colIndex, value)

        return valueCount
    }
}
