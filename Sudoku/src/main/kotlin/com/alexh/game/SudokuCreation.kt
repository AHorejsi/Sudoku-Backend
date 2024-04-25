package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.unflatten
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
    NINE(9, 3, 3)
}

enum class Difficulty(
    val lowerBoundOfInitialGivens: Float,
    val upperBoundOfInitialGivens: Float,
    val lowerBoundOfInitialGivensPerNeighborhood: Float
) {
    BEGINNER(0.58f, 0.68f, 0.55f),
    EASY(0.44f, 0.57f, 0.44f),
    MEDIUM(0.40f, 0.43f, 0.33f),
    HARD(0.35f, 0.38f, 0.22f),
    MASTER(0.21f, 0.33f, 0.0f)
}

data class MakeSudokuCommand(
    val dimension: Dimension,
    val difficulty: Difficulty,
    val games: Set<Game>,
    val random: Random = Random.Default
)

@Serializable
internal class Cage(val sum: Int, val positions: MutableSet<Position>)

internal class SudokuNode(
    val neighbors: MutableSet<SudokuNode>,
    val place: Position,
    var value: Int? = null
)

@Serializable
class SudokuJson private constructor(
    private val board: List<List<Int?>>,
    private val solved: List<List<Int>>,
    private val length: Int,
    private val games: Set<Game>,
    private val difficulty: String
) {
    internal constructor(
        info: MakeSudokuCommand,
        neighborhoods: List<SudokuNode>,
        solved: List<Int>
    ) : this(
        neighborhoods.map{ it.value }.unflatten(info.dimension.length),
        solved.unflatten(info.dimension.length),
        info.dimension.length,
        info.games,
        info.difficulty.name
    )
}

fun makeSudoku(info: MakeSudokuCommand): SudokuJson {
    val neighborhoods = initializeBoard(info)
    initializeValues(neighborhoods, info)
    val solved = neighborhoods.map{ it.value!! }
    adjustForDifficulty(neighborhoods, info)

    return SudokuJson(info, neighborhoods, solved)
}
