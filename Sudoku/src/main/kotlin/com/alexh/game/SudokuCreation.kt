package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.get2d
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
    FOUR(4, 2, 2),
    SIX(6, 2, 3),
    EIGHT(8, 4, 2),
    NINE(9, 3, 3),
    TEN(10, 2, 5),
    TWELVE(12, 3, 4),
    FIFTEEN(15, 5, 3),
    SIXTEEN(16, 4, 4)
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

internal interface SudokuCellNode {
    val index: Position

    val neighbors: Set<SudokuCellNode>

    var value: Int?
}
internal class MutableSudokuCellNode(
    override val index: Position,
    override val neighbors: MutableSet<MutableSudokuCellNode>,
    override var value: Int? = null
) : SudokuCellNode

@Serializable
class SudokuJson(
    private val table: List<List<Int?>>,
    private val length: Int,
    private val games: Set<String>,
    private val difficulty: String
) {
    companion object {
        internal fun from(info: MakeSudokuCommand, neighborhoods: List<SudokuCellNode>): SudokuJson {
            val length = info.dimension.length
            val difficulty = info.difficulty.name
            val games = info.games.map{ it.toString() }.toSet()
            val table = SudokuJson.makeTable(length, neighborhoods)

            return SudokuJson(table, length, games, difficulty)
        }

        private fun makeTable(length: Int, neighborhoods: List<SudokuCellNode>): List<List<Int?>> {
            val table = mutableListOf<List<Int?>>()
            val range = 0 until length

            for (rowIndex in range) {
                val row = mutableListOf<Int?>()

                for (colIndex in range) {
                    val value = get2d(rowIndex, colIndex, length, neighborhoods).value

                    row.add(value)
                }

                table.add(row)
            }

            return table
        }
    }
}

fun makeSudoku(info: MakeSudokuCommand): SudokuJson {
    val neighborhoods = initializeBoard(info)

    initializeValues(neighborhoods, info)
    // initializeCages()
    // adjustForDifficulty()
    // shuffleBoard()

    return SudokuJson.from(info, neighborhoods)
}
