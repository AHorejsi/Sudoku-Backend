package com.alexh

import com.alexh.game.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class SudokuTest {
    @Test
    fun testMakeSudoku() {
        val difficulties = Difficulty.values()
        val rand = Random(0)

        for (difficulty in difficulties) {
            this.testMakeSudokuHelper(Dimension.NINE, difficulty, rand)
        }
    }

    private fun testMakeSudokuHelper(dimension: Dimension, difficulty: Difficulty, rand: Random) {
        val games = Game.values()

        for (startIndex in games.indices) {
            for (endIndex in startIndex until games.size) {
                val selectedGames = games.sliceArray(startIndex until endIndex).toSet()

                val info = MakeSudokuCommand(dimension, difficulty, selectedGames, rand)
                val sudoku = makeSudoku(info)

                this.testSudoku(sudoku)
            }
        }
    }

    private fun testSudoku(sudoku: SudokuJson) {
        this.checkIfMatching(sudoku)
        this.checkIfValuesAreValid(sudoku)
        this.checkIfCagesAreValid(sudoku)
    }

    private fun checkIfMatching(sudoku: SudokuJson) {
        val range = 0 until sudoku.length

        for (rowIndex in range) {
            for (colIndex in range) {
                val value1 = sudoku.board[rowIndex][colIndex]
                val value2 = sudoku.solved[rowIndex][colIndex]

                if (null !== value1) {
                    assertEquals(value1, value2)
                }
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
        val set = mutableSetOf<Int>()

        for (colIndex in range) {
            set.add(sudoku.solved[rowIndex][colIndex])
        }

        assertEquals(sudoku.length, set.size)
    }

    private fun checkIfColumnIsValid(sudoku: SudokuJson, colIndex: Int, range: IntRange) {
        val set = mutableSetOf<Int>()

        for (rowIndex in range) {
            set.add(sudoku.solved[rowIndex][colIndex])
        }

        assertEquals(sudoku.length, set.size)
    }

    private fun checkIfBoxIsValid(sudoku: SudokuJson, box: Box) {
        val set = mutableSetOf<Int>()

        for (pos in box.positions) {
            set.add(sudoku.solved[pos.rowIndex][pos.colIndex])
        }

        assertEquals(sudoku.length, set.size)
    }

    private fun checkIfCagesAreValid(sudoku: SudokuJson) {
        sudoku.cages?.let { cageSet ->
            for (cage in cageSet) {
                val actualSum = cage.positions.sumOf{ pos -> sudoku.solved[pos.rowIndex][pos.colIndex] }

                assertEquals(cage.sum, actualSum)
            }
        }
    }
}
