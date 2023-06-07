package com.alexh.game

import kotlin.random.Random

private typealias Shuffler = (RegularSudoku, Random) -> Unit

fun shuffleBoardOfRegular(puzzle: RegularSudoku, rand: Random) {
    val shuffleTypes = mutableListOf<Shuffler>(::inner, ::flip, ::flipBox, ::rotate)

    for (count in 0 until 5) {
        shuffleTypes.shuffle(rand)

        for (shuffler in shuffleTypes) {
            shuffler(puzzle, rand)
        }
    }
}

private fun inner(puzzle: RegularSudoku, rand: Random) {
    innerByRow(puzzle, rand)
    innerByCol(puzzle, rand)
}

private fun innerByRow(puzzle: RegularSudoku, rand: Random) {
    val boxRows = puzzle.boxRows
    val length = puzzle.length

    for (startIndex in 0 until length step boxRows) {
        val lastIndex = startIndex + boxRows

        for (shuffleIndex in startIndex until lastIndex - 1) {
            val randIndex = rand.nextInt(shuffleIndex, lastIndex)

            if (shuffleIndex != randIndex) {
                swapRows(puzzle, length, shuffleIndex, randIndex)
            }
        }
    }
}

private fun swapRows(puzzle: RegularSudoku, length: Int, rowIndex1: Int, rowIndex2: Int) {
    for (colIndex in 0 until length) {
        val temp = puzzle.getValue(rowIndex1, colIndex)
        puzzle.setValue(rowIndex1, colIndex, puzzle.getValue(rowIndex2, colIndex))
        puzzle.setValue(rowIndex2, colIndex, temp)
    }
}

private fun innerByCol(puzzle: RegularSudoku, rand: Random) {
    val boxCols = puzzle.boxCols
    val length = puzzle.length

    for (startIndex in 0 until length step boxCols) {
        val lastIndex = startIndex + boxCols

        for (shuffleIndex in startIndex until lastIndex - 2) {
            val randIndex = rand.nextInt(shuffleIndex, lastIndex)

            if (shuffleIndex != randIndex) {
                swapCols(puzzle, length, shuffleIndex, randIndex)
            }
        }
    }
}

private fun swapCols(puzzle: RegularSudoku, length: Int, colIndex1: Int, colIndex2: Int) {
    for (rowIndex in 0 until length) {
        val temp = puzzle.getValue(rowIndex, colIndex1)
        puzzle.setValue(rowIndex, colIndex1, puzzle.getValue(rowIndex, colIndex2))
        puzzle.setValue(rowIndex, colIndex2, temp)
    }
}

private fun flipBox(puzzle: RegularSudoku, rand: Random) {
    val length = puzzle.length

    flipBoxByRow(puzzle, length, rand)
    flipBoxByCol(puzzle, length, rand)
}

private fun flipBoxByRow(puzzle: RegularSudoku, length: Int, rand: Random) {
    val rowBoxCount = puzzle.length / puzzle.boxRows

    for (rowBoxIndex in 0 until rowBoxCount) {
        val randRowBoxIndex = rand.nextInt(rowBoxIndex, rowBoxCount)

        if (rowBoxIndex != randRowBoxIndex) {
            swapRowBoxes(puzzle, length, rowBoxIndex, randRowBoxIndex)
        }
    }
}

private fun swapRowBoxes(puzzle: RegularSudoku, length: Int, rowBoxIndex1: Int, rowBoxIndex2: Int) {
    val boxRows = puzzle.boxRows
    val startRowIndex1 = rowBoxIndex1 * boxRows
    val startRowIndex2 = rowBoxIndex2 * boxRows

    for (count in 0 until boxRows) {
        val rowIndex1 = startRowIndex1 + count
        val rowIndex2 = startRowIndex2 + count

        for (colIndex in 0 until length) {
            val temp = puzzle.getValue(rowIndex1, colIndex)
            puzzle.setValue(rowIndex1, colIndex, puzzle.getValue(rowIndex2, colIndex))
            puzzle.setValue(rowIndex2, colIndex, temp)
        }
    }
}

private fun flipBoxByCol(puzzle: RegularSudoku, length: Int, rand: Random) {
    val colBoxCount = puzzle.length / puzzle.boxCols

    for (colBoxIndex in 0 until colBoxCount) {
        val randColBoxIndex = rand.nextInt(colBoxIndex, colBoxCount)

        if (colBoxIndex != randColBoxIndex) {
            swapColBoxes(puzzle, length, colBoxIndex, randColBoxIndex)
        }
    }
}

private fun swapColBoxes(puzzle: RegularSudoku, length: Int, colBoxIndex1: Int, colBoxIndex2: Int) {
    val boxCols = puzzle.boxCols
    val startColIndex1 = colBoxIndex1 * boxCols
    val startColIndex2 = colBoxIndex2 * boxCols

    for (count in 0 until boxCols) {
        val colIndex1 = startColIndex1 + count
        val colIndex2 = startColIndex2 + count

        for (rowIndex in 0 until length) {
            val temp = puzzle.getValue(rowIndex, colIndex1)
            puzzle.setValue(rowIndex, colIndex1, puzzle.getValue(rowIndex, colIndex2))
            puzzle.setValue(rowIndex, colIndex2, temp)
        }
    }
}

private fun flip(puzzle: RegularSudoku, rand: Random) {
    val length = puzzle.length

    if (rand.nextBoolean()) {
        horizontalFlip(puzzle, length)
    }
    if (rand.nextBoolean()) {
        verticalFlip(puzzle, length)
    }
}

private fun horizontalFlip(puzzle: RegularSudoku, length: Int) {
    var rowIndex1 = 0
    var rowIndex2 = length - 1

    while (rowIndex1 < rowIndex2) {
        for (colIndex in 0 until length) {
            val temp = puzzle.getValue(rowIndex1, colIndex)
            puzzle.setValue(rowIndex1, colIndex, puzzle.getValue(rowIndex2, colIndex))
            puzzle.setValue(rowIndex2, colIndex, temp)
        }

        ++rowIndex1
        --rowIndex2
    }
}

private fun verticalFlip(puzzle: RegularSudoku, length: Int) {
    var colIndex1 = 0
    var colIndex2 = length - 1

    while (colIndex1 < colIndex2) {
        for (rowIndex in 0 until length) {
            val temp = puzzle.getValue(rowIndex, colIndex1)
            puzzle.setValue(rowIndex, colIndex1, puzzle.getValue(rowIndex, colIndex2))
            puzzle.setValue(rowIndex, colIndex2, temp)
        }

        ++colIndex1
        --colIndex2
    }
}

private fun rotate(puzzle: RegularSudoku, rand: Random) {
    if (puzzle.boxRows == puzzle.boxCols) {
        when (rand.nextInt(4)) {
            0 -> rotate90(puzzle)
            1 -> rotate180(puzzle)
            2 -> rotate270(puzzle)
            3 -> return
        }
    }
    else {
        if (rand.nextBoolean()) {
            rotate180(puzzle)
        }
    }
}

private fun rotate90(puzzle: RegularSudoku) {
    val length = puzzle.length

    for (i in 0 until length / 2) {
        val x = length - 1 - i

        for (j in i until x) {
            val y = length - 1 - j

            val temp = puzzle.getValue(i, j)
            puzzle.setValue(i, j, puzzle.getValue(j, x))
            puzzle.setValue(j, x, puzzle.getValue(x, y))
            puzzle.setValue(x, y, puzzle.getValue(y, i))
            puzzle.setValue(y, i, temp)
        }
    }
}

private fun rotate180(puzzle: RegularSudoku) {
    val length = puzzle.length

    for (i in 0 until length / 2) {
        val x = length - i - 1

        for (j in 0 until length) {
            val y = length - j - 1

            val temp = puzzle.getValue(i, j)
            puzzle.setValue(i, j, puzzle.getValue(x, y))
            puzzle.setValue(x, y, temp)
        }
    }

    if (1 == length % 2) {
        reverseMiddleRow(puzzle, length)
    }
}

private fun reverseMiddleRow(puzzle: RegularSudoku, length: Int) {
    val middleRowIndex = length / 2
    var lowColIndex = 0
    var highColIndex = length - 1

    while (lowColIndex < highColIndex) {
        val temp = puzzle.getValue(middleRowIndex, lowColIndex)
        puzzle.setValue(middleRowIndex, lowColIndex, puzzle.getValue(middleRowIndex, highColIndex))
        puzzle.setValue(middleRowIndex, highColIndex, temp)

        ++lowColIndex
        --highColIndex
    }
}

private fun rotate270(puzzle: RegularSudoku) {
    val length = puzzle.length

    for (i in 0 until length / 2) {
        val x = length - 1 - i

        for (j in 0 until length) {
            val y = length - 1 - j

            val temp = puzzle.getValue(i, j)
            puzzle.setValue(i, j, puzzle.getValue(y, i))
            puzzle.setValue(y, i, puzzle.getValue(x, y))
            puzzle.setValue(x, y, puzzle.getValue(j, x))
            puzzle.setValue(j, x, temp)
        }
    }
}
