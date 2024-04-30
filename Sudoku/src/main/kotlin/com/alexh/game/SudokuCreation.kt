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
    val initialGivenLowerBound: Float,
    val initialGivenUpperBound: Float,
    val initialGivensPerNeighborhood: Float,
    val minCageSize: Float,
    val maxCageSize: Float
) {
    BEGINNER(0.58f, 0.68f, 0.55f, 0.22f, 0.44f),
    EASY(0.44f, 0.57f, 0.44f, 0.33f, 0.55f),
    MEDIUM(0.40f, 0.43f, 0.33f, 0.44f, 0.66f),
    HARD(0.35f, 0.38f, 0.22f, 0.55f, 0.77f),
    MASTER(0.21f, 0.33f, 0.0f, 0.66f, 0.88f)
}

data class MakeSudokuCommand(
    val dimension: Dimension,
    val difficulty: Difficulty,
    val games: Set<Game>,
    val random: Random = Random.Default
)

internal class SudokuNode(
    val neighbors: MutableSet<SudokuNode>,
    val place: Position,
    var value: Int? = null
)

@Serializable
class Cage(
    val sum: Int,
    val positions: MutableSet<Position>
)

@Serializable
class SudokuJson(
    val board: List<List<Int?>>,
    val solved: List<List<Int>>,
    val cages: Set<Cage>?,
    val length: Int,
    val difficulty: Difficulty,
    val games: Set<Game>,
)

fun makeSudoku(info: MakeSudokuCommand): SudokuJson {
    val length = info.dimension.length
    val difficulty = info.difficulty
    val games = info.games

    val neighborhoods = initializeBoard(info)
    initializeValues(neighborhoods, info)
    val solved = neighborhoods.map{ it.value!! }.unflatten(length)
    adjustForDifficulty(neighborhoods, info)
    val board = neighborhoods.map{ it.value }.unflatten(length)
    val cages = makeCages(solved, info)

    return SudokuJson(board, solved, cages, length, difficulty, games)
}
