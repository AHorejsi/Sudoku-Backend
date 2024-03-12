package com.alexh.game

import com.alexh.utils.actualIndex
import com.alexh.utils.checkBounds
import com.alexh.utils.checkLegal
import kotlinx.serialization.Serializable
import kotlin.random.Random

enum class Game {
    KILLER,
    HYPER,
    JIGSAW
}

enum class Dimension(
    val length: Int,
    val boxRows: Int,
    val boxCols: Int
) {
    NINE(9, 3, 3),
    TEN(10, 2, 5),
    TWELVE(12, 3, 4),
    FIFTEEN(15, 5, 3),
    SIXTEEN(16, 4, 4),
    EIGHTEEN(18, 3, 6),
    TWENTY(20, 4, 5),
    TWENTY_TWO(22, 11, 2),
    TWENTY_FOUR(24, 6, 4),
    TWENTY_FIVE(25, 5, 5);
}

enum class Difficulty(
    val lowerBoundOfInitialGivens: Float,
    val upperBoundOfInitialGivens: Float,
    val lowerBoundOfInitialGivensPerNeighborhood: Float
) {
    BEGINNER(0.58f, 0.68f, 0.55f),
    EASY(0.44f, 0.57f, 0.44f),
    MEDIUM(0.40f, 0.43f, 0.33f),
    HARD(0.34f, 0.39f, 0.22f),
    MASTER(0.21f, 0.33f, 0.0f)
}

data class MakeSudokuCommand(
    val dimension: Dimension,
    val difficulty: Difficulty,
    val games: Set<Game>,
    val random: Random = Random.Default
)

internal class NeighborNode(var value: Int?, val neighbors: MutableSet<NeighborNode>)

class SudokuCreator private constructor(
    private val info: MakeSudokuCommand,
    private val neighborhoods: MutableList<NeighborNode>
) {
    companion object {
        fun make(info: MakeSudokuCommand): SudokuJson {
            val neighborhoods = initializeBoard(info)
            val maker = SudokuCreator(info, neighborhoods)

            // initializeValues(this)
            // initializeCages(this)
            // adjustForDifficulty(this)
            // shuffleBoard(this)

            return maker.toJson()
        }
    }

    private val length: Int = this.info.dimension.length

    internal fun isSafe(rowIndex: Int, colIndex: Int, value: Int): Boolean {
        val length = this.length

        checkBounds(rowIndex, colIndex, length, length)
        checkLegal(value, length)

        val actualIndex = actualIndex(rowIndex, colIndex, length)
        val node = this.neighborhoods[actualIndex]

        for (neighbor in node.neighbors) {
            if (neighbor.value == value) {
                return false
            }
        }

        return true
    }

    internal fun getValue(rowIndex: Int, colIndex: Int): Int? {
        val length = this.length

        checkBounds(rowIndex, colIndex, length, length)

        val actualIndex = actualIndex(rowIndex, colIndex, length)

        return this.neighborhoods[actualIndex].value
    }

    internal fun setValue(rowIndex: Int, colIndex: Int, newValue: Int?) {
        val length = this.length

        checkBounds(rowIndex, colIndex, length, length)
        checkLegal(newValue, length)

        val actualIndex = actualIndex(rowIndex, colIndex, length)

        this.neighborhoods[actualIndex].value = newValue
    }

    internal fun deleteValue(rowIndex: Int, colIndex: Int) = this.setValue(rowIndex, colIndex, null)

    private fun toJson(): SudokuJson {
        val table = this.makeTable()
        val length = this.length
        val games = this.info.games.map{ it.toString() }.toSet()
        val difficulty = this.info.difficulty.toString()

        return SudokuJson(table, length, games, difficulty)
    }

    private fun makeTable(): List<List<Int?>> {
        val range = 0 until this.length

        val table = mutableListOf<List<Int?>>()

        for (rowIndex in range) {
            val row = mutableListOf<Int?>()

            for (colIndex in range) {
                val value = this.getValue(rowIndex, colIndex)

                row.add(value)
            }

            table.add(row)
        }

        return table
    }
}

@Serializable
class SudokuJson(
    private val table: List<List<Int?>>,
    private val length: Int,
    private val games: Set<String>,
    private val difficulty: String
)
