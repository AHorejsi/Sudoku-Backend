package com.alexh

import com.alexh.asserts.assertLess
import com.alexh.game.*
import com.alexh.utils.typeName
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.test.*

class SudokuTest {
    @Test
    fun testMakeSudoku() {
        val rand = Random(0)
        val name = rand.typeName()

        runBlocking(Dispatchers.IO) {
            val testCount = 10

            repeat(testCount) { count ->
                val testJob = this.launch { this@SudokuTest.runTest(rand, this) }

                setJobAsserts(testJob, "TEST $count of $name")
            }
        }
    }

    private fun runTest(rand: Random, scope: CoroutineScope) {
        for (dimension in Dimension.values()) {
            for (difficulty in Difficulty.values()) {
                this@SudokuTest.testMakeSudokuHelper0(scope, dimension, difficulty, rand)
            }
        }
    }

    private fun testMakeSudokuHelper0(
        scope: CoroutineScope,
        dimension: Dimension,
        difficulty: Difficulty,
        rand: Random
    ) {
        val games = Game.values()

        for (startIndex in games.indices) {
            for (endIndex in startIndex .. games.size) {
                val selectedGames =
                    games.sliceArray(startIndex until endIndex).toSortedSet()

                executeJob(scope, dimension, difficulty, selectedGames, rand)
            }
        }
    }

    private fun executeJob(
        scope: CoroutineScope,
        dimension: Dimension,
        difficulty: Difficulty,
        games: Set<Game>,
        rand: Random
    ) {
        val sudokuJob = scope.launch {
            val info = MakeSudokuCommand(dimension, difficulty, games, rand)
            val result = makeSudoku(info)

            this@SudokuTest.testSudokuProperties(result)
        }

        setJobAsserts(sudokuJob, "SUDOKU ($dimension, $difficulty, $games)")
    }

    private fun testSudokuProperties(sudoku: SudokuJson) {
        val description = sudoku.description
        val length = description.dimension.length

        this.checkIfCellsAreValid(sudoku, length)
        this.checkIfValuesAreValid(sudoku, length)
        this.checkIfCagesAreValid(sudoku, length)

        if (Game.HYPER in description.games) {
            val hyperBoxesPresent = sudoku.boxes.any(Box::isHyper)

            assertTrue(hyperBoxesPresent)
        }
    }

    private fun checkIfCellsAreValid(sudoku: SudokuJson, length: Int) {
        val range = 0 until length
        var nullCount = 0

        for (rowIndex in range) {
            for (colIndex in range) {
                val cell = sudoku.board[rowIndex][colIndex]
                val value = sudoku.solved[rowIndex][colIndex]

                if (null === cell.value) {
                    assertTrue(cell.editable)
                    ++nullCount
                } else {
                    assertFalse(cell.editable)
                    assertEquals(value, cell.value)
                }
                assertEquals(0, cell.notes)
            }
        }

        assertLess(0, nullCount)
    }

    private fun checkIfValuesAreValid(sudoku: SudokuJson, length: Int) {
        val range = 0 until length

        for (rowIndex in range) {
            this.checkIfRowIsValid(sudoku, rowIndex, range, length)
        }

        for (colIndex in range) {
            this.checkIfColumnIsValid(sudoku, colIndex, range, length)
        }

        for (box in sudoku.boxes) {
            this.checkIfBoxIsValid(sudoku, box, length)
        }
    }

    private fun checkIfRowIsValid(sudoku: SudokuJson, rowIndex: Int, range: IntRange, length: Int) {
        val set = HashSet<Int>(length)

        for (colIndex in range) {
            val value = sudoku.solved[rowIndex][colIndex]

            set.add(value)
        }

        assertEquals(length, set.size)
    }

    private fun checkIfColumnIsValid(sudoku: SudokuJson, colIndex: Int, range: IntRange, length: Int) {
        val set = HashSet<Int>(length)

        for (rowIndex in range) {
            val value = sudoku.solved[rowIndex][colIndex]

            set.add(value)
        }

        assertEquals(length, set.size)
    }

    private fun checkIfBoxIsValid(sudoku: SudokuJson, box: Box, length: Int) {
        val set = HashSet<Int>(length)

        for (pos in box.positions) {
            val value = sudoku.solved[pos.rowIndex][pos.colIndex]

            set.add(value)
        }

        assertEquals(length, set.size)
    }

    private fun checkIfCagesAreValid(sudoku: SudokuJson, length: Int) {
        val cageSet = sudoku.cages

        if (null === cageSet) {
            return
        }

        for (cage in cageSet) {
            val actualSum = cage.positions.sumOf{ pos -> sudoku.solved[pos.rowIndex][pos.colIndex] }

            assertEquals(cage.sum, actualSum)
        }

        val actualCellCount = length * length
        val cellCountFromCages = cageSet.sumOf{ it.positions.size }

        assertEquals(actualCellCount, cellCountFromCages)
    }
}

private fun setJobAsserts(job: Job, message: String) {
    job.invokeOnCompletion { ex ->
        println("Finished: $message")

        assertNull(ex)

        assertTrue(job.isCompleted)
        assertFalse(job.isCancelled)
    }
}
