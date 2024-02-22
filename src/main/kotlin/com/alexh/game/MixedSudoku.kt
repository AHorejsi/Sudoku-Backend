package com.alexh.game

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

@Serializable
enum class Game {
    KILLER,
    HYPER,
    JIGSAW
}

@Serializable
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
    TWENTY_FIVE(25, 5, 5)
}

@Serializable
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

@Serializable
data class MakeSudokuCommand(
    val dimension: Dimension,
    val difficulty: Difficulty,
    val games: Set<Game>,
    @Transient val random: Random = Random.Default
)

@Serializable
internal class NeighborNode(var value: Int?, val neighbors: MutableSet<NeighborNode>)

@Serializable
class MixedSudoku(private val info: MakeSudokuCommand) {
    private val neighborhoods: MutableList<NeighborNode> = initializeBoard(this.info)

    init {
        // initialize values
        // initialize cages
        // adjust for difficulties
        // shuffle board
    }
}
