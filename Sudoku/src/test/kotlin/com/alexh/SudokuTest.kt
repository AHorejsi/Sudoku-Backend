package com.alexh

import com.alexh.asserts.assertLess
import com.alexh.game.*
import com.alexh.utils.SyncRandom
import com.alexh.utils.subsets
import com.alexh.utils.typeName
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.test.*

class SudokuTest {
    @Test
    fun testMakeSudoku() {
        val seed = 0L
        val rngSet = listOf(Random(seed), SyncRandom(seed))

        for (rand in rngSet) {
            runBlocking(Dispatchers.IO) {
                val rngName = rand.typeName()
                val rngJob = this.launch { this@SudokuTest.initializeTest(rand, rngName, this) }

                this@SudokuTest.setJobAsserts(rngJob, "Completed all tests for $rngName\n\n")
            }
        }
    }

    private fun initializeTest(rand: Random, rngName: String, scope: CoroutineScope) {
        val testCount = 10

        repeat(testCount) { count ->
            val testCounter = count + 1
            val testJob = scope.launch { this@SudokuTest.runTest(rand, this, testCounter, rngName) }

            this@SudokuTest.setJobAsserts(testJob, "Finished TEST $testCounter of $rngName")
        }
    }

    private fun runTest(rand: Random, scope: CoroutineScope, testCounter: Int, rngName: String) {
        val dimensionArray = Dimension.values()
        val difficultyArray = Difficulty.values()
        val gameSubsets = Game.values().subsets()

        for (dimension in dimensionArray) {
            for (difficulty in difficultyArray) {
                for (games in gameSubsets) {
                    val gameSet = games.toSortedSet()

                    this.executeJob(scope, dimension, difficulty, gameSet, rand, testCounter, rngName)
                }
            }
        }
    }

    private fun executeJob(
        scope: CoroutineScope,
        dimension: Dimension,
        difficulty: Difficulty,
        games: Set<Game>,
        rand: Random,
        testCounter: Int,
        rngName: String
    ) {
        val sudokuJob = scope.launch {
            val info = MakeSudokuCommand(dimension, difficulty, games, rand)
            val result = makeSudoku(info)

            this@SudokuTest.testSudokuProperties(result)
        }

        this.setJobAsserts(
            sudokuJob, "TEST $testCounter of $rngName: Rules ($dimension, $difficulty, $games) GENERATED"
        )
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

    private fun setJobAsserts(job: Job, message: String) {
        job.invokeOnCompletion { ex ->
            println(message)

            assertNull(ex)

            assertTrue(job.isCompleted)
            assertFalse(job.isCancelled)
        }
    }
}
