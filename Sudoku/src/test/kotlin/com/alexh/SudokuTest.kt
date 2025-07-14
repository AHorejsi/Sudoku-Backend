package com.alexh

import com.alexh.game.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SudokuTest {
    @Test
    fun testMakeSudoku() {
        val dimensionArray = Dimension.values()
        val difficultyArray = Difficulty.values()
        val rand = Random(0)

        for (dimension in dimensionArray) {
            for (difficulty in difficultyArray) {
                this.testMakeSudokuHelper(dimension, difficulty, rand)
            }
        }
    }

    private fun testMakeSudokuHelper(dimension: Dimension, difficulty: Difficulty, rand: Random) {
        val games = Game.values()

        for (startIndex in games.indices) {
            for (endIndex in startIndex .. games.size) {
                val selectedGames = games.sliceArray(startIndex until endIndex).toSet()

                val info = MakeSudokuCommand(dimension, difficulty, selectedGames, rand)
                val sudoku = makeSudoku(info)

                this.testSudokuProperties(sudoku)
            }
        }
    }

    private fun testSudokuProperties(sudoku: SudokuJson) {
        this.checkIfMatching(sudoku)
        this.checkIfValuesAreValid(sudoku)
        this.checkIfCagesAreValid(sudoku)
    }

    private fun checkIfMatching(sudoku: SudokuJson) {
        val range = 0 until sudoku.length

        for (rowIndex in range) {
            for (colIndex in range) {
                val value1 = sudoku.board[rowIndex][colIndex].value
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
            val result = set.add(sudoku.solved[rowIndex][colIndex])

            assertTrue(result)
        }

        assertEquals(sudoku.length, set.size)
    }

    private fun checkIfColumnIsValid(sudoku: SudokuJson, colIndex: Int, range: IntRange) {
        val set = mutableSetOf<Int>()

        for (rowIndex in range) {
            val result = set.add(sudoku.solved[rowIndex][colIndex])

            assertTrue(result)
        }

        assertEquals(sudoku.length, set.size)
    }

    private fun checkIfBoxIsValid(sudoku: SudokuJson, box: Box) {
        val set = mutableSetOf<Int>()

        for (pos in box.positions) {
            val result = set.add(sudoku.solved[pos.rowIndex][pos.colIndex])

            assertTrue(result)
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

        val cellCount = sudoku.length * sudoku.length
        assertEquals(cellCount, cageSet.sumOf{ it.positions.size })
    }
}
