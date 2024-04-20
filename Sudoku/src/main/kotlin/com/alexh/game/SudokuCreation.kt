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
    MASTER(0.05f, 0.12f, 0.0f)
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
    private val board: List<List<Int?>>,
    private val solved: List<List<Int>>,
    private val length: Int,
    private val games: Set<Game>,
    private val difficulty: String
) {
    constructor(
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
