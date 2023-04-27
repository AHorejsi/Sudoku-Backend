package com.alexh.game

internal fun initializeValues(puzzle: RegularSudoku) {
    val legal = puzzle.legal.toMutableList()

    initializeValuesHelper1(puzzle, legal)

    val valueMap = shuffleValues(legal, puzzle)
    val initial = Position(0, 0)

    initializeValuesHelper2(puzzle, valueMap, initial)

    assert(puzzle.solved)
}

private fun initializeValuesHelper1(puzzle: RegularSudoku, legal: MutableList<Int>) {
    val length = puzzle.length
    val boxRows = puzzle.boxRows
    val boxCols = puzzle.boxCols

    for (startRowIndex in 0 until length step boxRows) {
        for (startColIndex in 0 until length step boxCols) {
            val endRowIndex = startRowIndex + boxRows
            val endColIndex = startColIndex + boxCols

            legal.shuffle()

            assignValuesToBox(puzzle, legal, startRowIndex, startColIndex, endRowIndex, endColIndex)
        }
    }
}

private fun assignValuesToBox(
    puzzle: RegularSudoku,
    legal: List<Int>,
    startRowIndex: Int,
    startColIndex: Int,
    endRowIndex: Int,
    endColIndex: Int
) {
    var legalIndex = 0

    for (rowIndex in startRowIndex until endRowIndex) {
        for (colIndex in startColIndex until endColIndex) {
            puzzle.setValue(rowIndex, colIndex, legal[legalIndex])

            ++legalIndex
        }
    }
}

private fun shuffleValues(legal: MutableList<Int>, puzzle: RegularSudoku): Map<Position, List<Int>> {
    val length = legal.size
    val valueMap = mutableMapOf<Position, List<Int>>()

    for (rowIndex in 0 until length) {
        for (colIndex in 0 until length) {
            if (null === puzzle.getValue(rowIndex, colIndex)) {
                val copy = legal.shuffled()
                val pos = Position(rowIndex, colIndex)

                valueMap[pos] = copy
            }
        }
    }

    return valueMap
}

private fun initializeValuesHelper2(
    puzzle: RegularSudoku,
    valueMap: Map<Position, List<Int>>,
    prev: Position
): Boolean {
    val next = nextPosition(prev, puzzle)

    if (next.rowIndex == puzzle.length)
        return true

    val legal = valueMap.getValue(next)
    val rowIndex = next.rowIndex
    val colIndex = next.colIndex

    for (value in legal) {
        if (puzzle.isSafe(rowIndex, colIndex, value)) {
            puzzle.setValue(rowIndex, colIndex, value)

            if (initializeValuesHelper2(puzzle, valueMap, next))
                return true

            puzzle.deleteValue(rowIndex, colIndex)
        }
    }

    return false
}

private fun nextPosition(prev: Position, puzzle: RegularSudoku): Position {
    var rowIndex = prev.rowIndex
    var colIndex = prev.colIndex

    while (null !== puzzle.getValue(rowIndex, colIndex)) {
        ++colIndex

        if (colIndex == puzzle.length) {
            ++rowIndex
            colIndex = 0
        }
    }

    return Position(rowIndex, colIndex)
}
