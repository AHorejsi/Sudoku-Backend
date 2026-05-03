package com.alexh

import com.alexh.game.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SudokuTest {
    @Test
    fun testMakeSudoku() {
        val staticRng = Random(0)
        val testCount = 10

        val dimensionArray = Dimension.values()
        val difficultyArray = Difficulty.values()
        val gameArray = Game.values()

        repeat(testCount) {
            val seed = staticRng.nextInt()
            val rand = Random(seed)

            for (dimension in dimensionArray) {
                for (difficulty in difficultyArray) {
                    this.testMakeSudokuHelper(dimension, difficulty, gameArray, rand)
                }
            }
        }
    }

    private fun testMakeSudokuHelper(dimension: Dimension, difficulty: Difficulty, games: Array<Game>, rand: Random) {
        for (startIndex in games.indices) {
            for (endIndex in startIndex .. games.size) {
                val selectedGames = games.slice(startIndex until endIndex).toSet()

                val info = MakeSudokuCommand(dimension, difficulty, selectedGames, rand)
                val sudoku = makeSudoku(info)

                this.testSudokuProperties(sudoku)
            }
        }
    }

    private fun testSudokuProperties(sudoku: SudokuJson) {
        this.checkIfCellsAreValid(sudoku)
        this.checkIfValuesAreValid(sudoku)
        this.checkIfCagesAreValid(sudoku)

        if (Game.HYPER in sudoku.games) {
            val hyperBoxesPresent = sudoku.boxes.any{ it.isHyper }

            assertTrue(hyperBoxesPresent)
        }
    }

    private fun checkIfCellsAreValid(sudoku: SudokuJson) {
        val range = 0 until sudoku.length

        for (rowIndex in range) {
            for (colIndex in range) {
                val cell = sudoku.board[rowIndex][colIndex]
                val value = sudoku.solved[rowIndex][colIndex]

                if (null !== cell.value) {
                    assertEquals(value, cell.value)
                }
                assertEquals(0, cell.notes)
                assertEquals(cell.editable, null === cell.value)
            }
        }
    }

    private fun checkIfValuesAreValid(sudoku: SudokuJson) {
        val range = 0 until sudoku.length

        for (rowIndex in range) {
            this.checkIfRowIsValid(sudoku, rowIndex, range)
        }

        for (colIndex in range) {
            this.checkIfColumnIsValid(sudoku, colIndex, range)
        }

        for (box in sudoku.boxes) {
            this.checkIfBoxIsValid(sudoku, box)
        }
    }

    private fun checkIfRowIsValid(sudoku: SudokuJson, rowIndex: Int, range: IntRange) {
        val set = hashSetOf<Int>()

        for (colIndex in range) {
            val value = sudoku.solved[rowIndex][colIndex]

            val notDuplicate = set.add(value)

            assertTrue(notDuplicate)
        }

        assertEquals(sudoku.length, set.size)
    }

    private fun checkIfColumnIsValid(sudoku: SudokuJson, colIndex: Int, range: IntRange) {
        val set = hashSetOf<Int>()

        for (rowIndex in range) {
            val value = sudoku.solved[rowIndex][colIndex]

            val notDuplicate = set.add(value)

            assertTrue(notDuplicate)
        }

        assertEquals(sudoku.length, set.size)
    }

    private fun checkIfBoxIsValid(sudoku: SudokuJson, box: Box) {
        val set = hashSetOf<Int>()

        for (pos in box.positions) {
            val value = sudoku.solved[pos.rowIndex][pos.colIndex]

            val notDuplicate = set.add(value)

            assertTrue(notDuplicate)
        }

        assertEquals(sudoku.length, set.size)
    }

    private fun checkIfCagesAreValid(sudoku: SudokuJson) {
        val cageSet = sudoku.cages

        if (null === cageSet) {
            return
        }

        for (cage in cageSet) {
            val actualSum = cage.positions.sumOf{ pos -> sudoku.solved[pos.rowIndex][pos.colIndex] }

            assertEquals(cage.sum, actualSum)
        }

        val actualCellCount = sudoku.length * sudoku.length
        val cellCountFromCages = cageSet.sumOf{ it.positions.size }

        assertEquals(actualCellCount, cellCountFromCages)
    }
}
