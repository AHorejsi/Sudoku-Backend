package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.unflatten
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

enum class Game {
    HYPER,
    KILLER
}

enum class Dimension(
    val length: Int,
    val boxRows: Int,
    val boxCols: Int
) {
    FOUR(4, 2, 2),
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

@Serializable
data class MakeSudokuCommand(
    val dimension: Dimension,
    val difficulty: Difficulty,
    val games: Set<Game>,
    @Transient val random: Random = Random.Default
)

internal class SudokuNode(
    length: Int,
    val place: Position
) {
    private val _row: MutableSet<SudokuNode> = HashSet(length)
    private val _col: MutableSet<SudokuNode> = HashSet(length)
    private val _box: MutableSet<SudokuNode> = HashSet(length)
    private val _hyper: MutableSet<SudokuNode> = HashSet(length)
    private var _all: Set<SudokuNode> = emptySet()

    var value: Int? = null
    private var changed: Boolean = false

    val row: Iterable<SudokuNode>
        get() = this._row
    val col: Iterable<SudokuNode>
        get() = this._col
    val box: Iterable<SudokuNode>
        get() = this._box
    val hyper: Iterable<SudokuNode>
        get() = this._hyper
    val all: Iterable<SudokuNode>
        get() {
            if (this.changed) {
                this._all = this._row union this._col union this._box union this._hyper
                this.changed = false
            }

            return this._all
        }

    fun addToRowSet(other: SudokuNode): Boolean {
        if (this === other) {
            throw IllegalArgumentException("A node cannot have a connection to itself")
        }

        this.changed = this._row.add(other)

        return this.changed
    }

    fun addToColSet(other: SudokuNode): Boolean {
        if (this === other) {
            throw IllegalArgumentException("A node cannot have a connection to itself")
        }

        this.changed = this._col.add(other)

        return this.changed
    }

    fun addToBoxSet(other: SudokuNode): Boolean {
        if (this === other) {
            throw IllegalArgumentException("A node cannot have a connection to itself")
        }

        this.changed = this._box.add(other)

        return this.changed
    }

    @Suppress("UNUSED")
    fun removeFromBoxSet(other: SudokuNode): Boolean {
        this.changed = this._box.remove(other)

        return this.changed
    }

    fun addToHyperSet(other: SudokuNode): Boolean {
        if (this === other) {
            throw IllegalArgumentException("A node cannot have a connection to itself")
        }

        this.changed = this._hyper.add(other)

        return this.changed
    }

    @Suppress("UNUSED")
    fun removeFromHyperSet(other: SudokuNode): Boolean {
        this.changed = this._hyper.remove(other)

        return this.changed
    }
}

@Suppress("UNUSED")
@Serializable
class Cage(
    val sum: Int,
    val positions: MutableSet<Position>
)

@Suppress("UNUSED")
@Serializable
class Box(
    val isHyper: Boolean,
    val positions: MutableSet<Position>
)

@Suppress("UNUSED")
@Serializable
class SudokuJson(
    val board: List<List<Int?>>,
    val solved: List<List<Int>>,
    val cages: Set<Cage>?,
    val boxes: Set<Box>,
    val length: Int,
    val difficulty: Difficulty,
    val games: Set<Game>
)

fun makeSudoku(info: MakeSudokuCommand): SudokuJson {
    val length = info.dimension.length
    val difficulty = info.difficulty
    val games = info.games

    // Build table representing the sudoku and connect each cell in a graph
    val (neighborhoods, boxes) = initializeBoard(info)

    // Fill entire table with values
    initializeValues(neighborhoods, info)

    // Generate cages if killer sudoku is being played
    val cages = makeCages(neighborhoods, info)

    // Save solved state of the sudoku for later checking
    val solved = neighborhoods.map{ it.value!! }.unflatten(length)

    // Remove values from the sudoku in such a way that ensures there is only one solution
    adjustForDifficulty(neighborhoods, info)

    // Save unsolved state of the sudoku for playing
    val board = neighborhoods.map{ it.value }.unflatten(length)

    // Save all above information as JSON
    return SudokuJson(board, solved, cages, boxes, length, difficulty, games)
}
