package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.unflatten
import kotlinx.serialization.Serializable
import kotlin.random.Random

enum class Game {
    HYPER,
    JIGSAW,
    KILLER
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
    length: Int,
    val place: Position
) {
    val row: MutableSet<SudokuNode> = HashSet(length)
    val col: MutableSet<SudokuNode> = HashSet(length)
    val box: MutableSet<SudokuNode> = HashSet(length)
    val hyper: MutableSet<SudokuNode> = HashSet(length)
    val all: MutableSet<SudokuNode> = HashSet(4 * length)
    var value: Int? = null

    fun addToRowSet(other: SudokuNode) {
        if (this === other) {
            throw IllegalArgumentException("A node cannot have a connection to itself")
        }

        this.row.add(other)
        this.all.add(other)
    }

    fun addToColSet(other: SudokuNode) {
        if (this === other) {
            throw IllegalArgumentException("A node cannot have a connection to itself")
        }

        this.col.add(other)
        this.all.add(other)
    }

    fun addToBoxSet(other: SudokuNode) {
        if (this === other) {
            throw IllegalArgumentException("A node cannot have a connection to itself")
        }

        this.box.add(other)
        this.all.add(other)
    }

    fun addToHyperSet(other: SudokuNode) {
        if (this === other) {
            throw IllegalArgumentException("A node cannot have a connection to itself")
        }

        this.hyper.add(other)
        this.all.add(other)
    }
}

@Serializable
class Cage(
    val sum: Int,
    val positions: Set<Position>
)

@Serializable
class Box(
    val positions: Set<Position>,
    val isHyper: Boolean
)

@Serializable
class SudokuJson(
    val board: List<List<Int?>>,
    val solved: List<List<Int>>,
    val cages: Set<Cage>?,
    val boxes: Set<Box>,
    val length: Int,
    val difficulty: Difficulty,
    val games: Set<Game>,
)

fun makeSudoku(info: MakeSudokuCommand): SudokuJson {
    val length = info.dimension.length
    val difficulty = info.difficulty
    val games = info.games

    val (neighborhoods, boxes) = initializeBoard(info)
    initializeValues(neighborhoods, info)
    val solved = neighborhoods.map{ it.value!! }.unflatten(length)
    adjustForDifficulty(neighborhoods, info)
    val board = neighborhoods.map{ it.value }.unflatten(length)
    val cages = makeCages(solved, info)

    return SudokuJson(board, solved, cages, boxes, length, difficulty, games)
}
