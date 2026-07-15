package com.alexh

import com.alexh.game.*
import com.alexh.asserts.assertGreater
import com.alexh.utils.*
import kotlinx.coroutines.*
import kotlin.collections.HashSet
import kotlin.random.Random
import kotlin.test.*

class SudokuTest {
    @Test
    fun testMakeSudoku() {
        val rngSet = this.makeRngSet()

        for (rand in rngSet) {
            runBlocking(Dispatchers.IO) {
                this.launch {
                    this@SudokuTest.initializeTest(rand, this)
                }.also { rngJob ->
                    this@SudokuTest.setJobAsserts(rngJob, "Completed all tests for ${rand.typeName()}\n\n")
                }
            }
        }
    }

    private fun makeRngSet(): List<Random> {
        val rand1 = Random(0)
        val rand2 = SyncRandom(0)
        val rand3 = Random.Default

        return listOf(rand1, rand2, rand3)
    }

    private fun initializeTest(rand: Random, scope: CoroutineScope) {
        val testCount = 10

        repeat(testCount) { count ->
            scope.launch {
                this@SudokuTest.runTest(rand, this, count)
            }.also { testJob ->
                this@SudokuTest.setJobAsserts(testJob, "FINISHED TEST $count of ${rand.typeName()}")
            }
        }
    }

    private fun runTest(rand: Random, scope: CoroutineScope, testCounter: Int) {
        val dimensionArray = Dimension.states
        val difficultyArray = Difficulty.states
        val gameSubsets = Game.subsets

        for (dimension in dimensionArray) {
            for (difficulty in difficultyArray) {
                for (games in gameSubsets) {
                    this.executeJob(scope, dimension, difficulty, games, rand, testCounter)
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
        testCounter: Int
    ) {
        scope.launch {
            val info = MakeSudokuCommand(dimension, difficulty, games, rand)
            val result = makeSudoku(info)

            this@SudokuTest.testSudokuProperties(result, info)
        }.also { sudokuJob ->
            val message = "TEST $testCounter of ${rand.typeName()}: RULES ($dimension, $difficulty, $games) GENERATED"

            this.setJobAsserts(sudokuJob, message)
        }
    }

    private fun testSudokuProperties(sudoku: SudokuJson, info: MakeSudokuCommand) {
        assertEquals(info, sudoku.description)

        val length = info.dimension.length
        this.checkIfCellsAreValid(sudoku, length)
        this.checkIfValuesAreValid(sudoku, length)
        this.checkIfCagesAreValid(sudoku, length)

        if (Game.HYPER in info.games) {
            val hyperBoxesPresent = sudoku.boxes.any(Box::isHyper)

            assertTrue(hyperBoxesPresent)
        }
    }

    private fun checkIfCellsAreValid(sudoku: SudokuJson, length: Int) {
        val range = 0 until length
        var nullCount = 0

        for ((rowIndex, colIndex) in range.pair(range)) {
            val cell = sudoku.board[rowIndex][colIndex]
            val value = sudoku.solved[rowIndex][colIndex]

            if (null === cell.value) {
                assertTrue(cell.editable)
                ++nullCount
            } else {
                assertFalse(cell.editable)
                assertEquals(value, cell.value)
            }

            val noNotes = cell.notes.isEmpty()
            assertTrue(noNotes)
        }

        assertGreater(nullCount, 0)
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

            val notDuplicate = set.add(value)

            assertTrue(notDuplicate)
        }
    }

    private fun checkIfColumnIsValid(sudoku: SudokuJson, colIndex: Int, range: IntRange, length: Int) {
        val set = HashSet<Int>(length)

        for (rowIndex in range) {
            val value = sudoku.solved[rowIndex][colIndex]

            val notDuplicate = set.add(value)

            assertTrue(notDuplicate)
        }
    }

    private fun checkIfBoxIsValid(sudoku: SudokuJson, box: Box, length: Int) {
        val set = HashSet<Int>(length)

        for (pos in box.positions) {
            val value = sudoku.solved[pos.rowIndex][pos.colIndex]

            val notDuplicate = set.add(value)

            assertTrue(notDuplicate)
        }
    }

    private fun checkIfCagesAreValid(sudoku: SudokuJson, length: Int) {
        val cageSet = sudoku.cages

        if (null === cageSet) {
            return
        }

        for (cage in cageSet) {
            val actualSum = cage.positions.sumOf { sudoku.solved[it.rowIndex][it.colIndex] }

            assertEquals(cage.sum, actualSum)
        }

        val actualCellCount = length * length
        val cellCountFromCages = cageSet.sumOf { it.positions.size }

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
