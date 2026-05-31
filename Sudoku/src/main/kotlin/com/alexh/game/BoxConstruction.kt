package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.up

internal fun constructBoxSet(dimension: Dimension, games: Set<Game>): Set<Box> {
    val boxSet = HashSet<Box>(dimension.length)

    makeRegularBoxes(dimension, boxSet)
    if (Game.HYPER in games) {
        makeHyperBoxes(dimension, boxSet)
    }

    return boxSet
}

private fun makeRegularBoxes(dimension: Dimension, boxSet: MutableSet<Box>) {
    val length = dimension.length
    val rowSize = dimension.boxRows
    val colSize = dimension.boxCols

    val range = 0 until length

    for (startRowIndex in range step rowSize) {
        val rowRange = startRowIndex up rowSize

        for (startColIndex in range step colSize) {
            val colRange = startColIndex up colSize
            val newBox = makeNewBox(rowRange, colRange, length, false)

            boxSet.add(newBox)
        }
    }
}

private fun makeHyperBoxes(dimension: Dimension, boxSet: MutableSet<Box>) {
    val length = dimension.length
    val rowSize = dimension.boxRows
    val colSize = dimension.boxCols

    val stepRow = rowSize + 1
    val stepCol = colSize + 1

    val range = 1 until (length - 1)

    for (startRowIndex in range step stepRow) {
        val rowRange = startRowIndex up rowSize

        for (startColIndex in range step stepCol) {
            val colRange = startColIndex up colSize
            val newBox = makeNewBox(rowRange, colRange, length, true)

            boxSet.add(newBox)
        }
    }
}

private fun makeNewBox(rowRange: IntRange, colRange: IntRange, length: Int, isHyper: Boolean): Box {
    val positionSet = HashSet<Position>(length)

    for (rowIndex in rowRange) {
        for (colIndex in colRange) {
            val newPos = Position(rowIndex, colIndex)

            positionSet.add(newPos)
        }
    }

    return Box(isHyper, positionSet)
}
