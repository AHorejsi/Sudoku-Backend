package com.alexh

import com.alexh.game.*
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SudokuTest {
    @Test
    fun testMakeSudoku() = runBlocking(Dispatchers.IO) {
        val testCount = 10
        val static = Random(0)

        repeat(testCount) { _ ->
            this@SudokuTest.testMakeSudokuHelper0(this, static)
        }
    }

    private fun testMakeSudokuHelper0(scope: CoroutineScope, static: Random) {
        for (dimension in Dimension.values()) {
            for (difficulty in Difficulty.values()) {
                this@SudokuTest.testMakeSudokuHelper1(scope, dimension, difficulty, static)
            }
        }
    }

    private fun testMakeSudokuHelper1(
        scope: CoroutineScope,
        dimension: Dimension,
        difficulty: Difficulty,
        static: Random
    ) {
        val seed = static.nextInt()
        val rand = Random(seed)

        scope.launch { this@SudokuTest.testMakeSudokuHelper2(dimension, difficulty, rand) }
    }

    private fun testMakeSudokuHelper2(
        dimension: Dimension,
        difficulty: Difficulty,
        rand: Random
    ) {
        val games = Game.values()

        for (startIndex in games.indices) {
            for (endIndex in startIndex .. games.size) {
                val selectedGames = games.sliceArray(startIndex until endIndex).toSet()

                val info = MakeSudokuCommand(dimension, difficulty, selectedGames, rand)
                val sudoku = makeSudoku(info)

                this@SudokuTest.testSudokuProperties(sudoku)
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

                if (null === cell.value) {
                    assertTrue(cell.editable)
                }
                else {
                    assertFalse(cell.editable)
                    assertEquals(value, cell.value)
                }
                assertEquals(0, cell.notes)
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
