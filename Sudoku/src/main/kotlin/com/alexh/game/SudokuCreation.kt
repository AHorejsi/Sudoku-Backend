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
    NINE(9, 3, 3),
    SIXTEEN(16, 4, 4)
}

enum class Difficulty(
    val lowerBoundOfInitialGivens: Float,
    val upperBoundOfInitialGivens: Float,
    val lowerBoundOfInitialGivensPerNeighborhood: Float
) {
    BEGINNER(0.40f, 0.47f, 0.44f),
    EASY(0.36f, 0.39f, 0.33f),
    MEDIUM(0.22f, 0.35f, 0.22f),
    HARD(0.13f, 0.21f, 0.11f),
    MASTER(0.0f, 0.12f, 0.0f)
}

data class MakeSudokuCommand(
    val dimension: Dimension,
    val difficulty: Difficulty,
    val games: Set<Game>,
    val random: Random = Random.Default
)

open class SudokuNode(
    open val neighbors: Set<SudokuNode>,
    open val place: Position,
    open var value: Int? = null
)
class MutableSudokuNode(
    override val neighbors: MutableSet<MutableSudokuNode>,
    override val place: Position,
    override var value: Int? = null
) : SudokuNode(neighbors, place, value)

@Serializable
class SudokuJson private constructor(
    val board: List<List<Int?>>,
    val length: Int,
    val games: Set<String>,
    val difficulty: String
) {
    constructor(
        info: MakeSudokuCommand,
        neighborhoods: List<SudokuNode>
    ) : this(
        SudokuJson.makeBoard(info.dimension.length, neighborhoods),
        info.dimension.length,
        info.games.asSequence().map{ it.toString() }.toSet(),
        info.difficulty.name
    )

    companion object {
        private fun makeBoard(
            length: Int,
            neighborhoods: List<SudokuNode>
        ): List<List<Int?>> {
            val table = mutableListOf<List<Int?>>()
            val range = 0 until length

            for (rowIndex in range) {
                val row = mutableListOf<Int?>()

                for (colIndex in range) {
                    val node = neighborhoods.get2d(rowIndex, colIndex, length)

                    row.add(node.value)
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
    adjustForDifficulty(neighborhoods, info)

    return SudokuJson(info, neighborhoods)
}
