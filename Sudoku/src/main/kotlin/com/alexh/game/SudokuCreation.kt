package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.unflatten
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

/** Modifiers to the rules of the Sudoku game */
enum class Game {
    HYPER,
    KILLER
}

/**
 * Data that correspond to the dimensions of the Sudoku board.
 * Lists the selectable dimensions that can be played
 */
enum class Dimension(
    /** Number of rows/columns in the Sudoku board */
    val length: Int,

    /** Number of rows in a box of the Sudoku board */
    val boxRows: Int,

    /** Number of columns in a box of the Sudoku board */
    val boxCols: Int
) {
    FOUR(4, 2, 2),
    NINE(9, 3, 3),
    SIXTEEN(16, 4, 4)
}

/**
 * Data that corresponds to the chosen difficulty level of the Sudoku board.
 * Lists the selectable difficulty level that can be played
 */
enum class Difficulty(
    /** Lower bound on the percentage of cells that must be filled after adjusting for difficulty level */
    val initialGivenLowerBound: Float,

    /** Upper bound on the percentage of cells that must be filled after adjusting for difficulty */
    val initialGivenUpperBound: Float,

    /** Lower bound on the percentage of cells per neighborhood that must be filled after adjusting for difficulty level */
    val initialGivensPerNeighborhood: Float,

    /** Lower bound on the percentage of cells to include in a single cage */
    val minCageSize: Float,

    /** Upper bound on the percentage of cells to include in a single cage */
    val maxCageSize: Float
) {
    BEGINNER(0.58f, 0.68f, 0.55f, 0.22f, 0.44f),
    EASY(0.44f, 0.57f, 0.44f, 0.33f, 0.55f),
    MEDIUM(0.40f, 0.43f, 0.33f, 0.44f, 0.66f),
    HARD(0.35f, 0.38f, 0.22f, 0.55f, 0.77f),
    MASTER(0.21f, 0.33f, 0.0f, 0.66f, 0.88f)
}

/** Chosen settings for generating a Sudoku board for gameplay */
@Serializable
data class MakeSudokuCommand(
    val dimension: Dimension,
    val difficulty: Difficulty,
    val games: Set<Game>,
    @Transient val random: Random = Random.Default
)

/** A single cell of a graph that represents a Sudoku board */
internal class SudokuNode(
    /** Coordinates of this node on the Sudoku board */
    val place: Position
) {
    /** Backing field for [row] property */
    private val _row: MutableSet<SudokuNode> = HashSet()

    /** Backing field for [column] property */
    private val _column: MutableSet<SudokuNode> = HashSet()

    /** Backing field for [box] property */
    private val _box: MutableSet<SudokuNode> = HashSet()

    /** Backing field for [hyper] property */
    private val _hyper: MutableSet<SudokuNode> = HashSet()

    /** Backing field for [all] property */
    private var _all: Set<SudokuNode> = emptySet()

    /** Tracks if the neighborhood of this node has been changed during board building */
    private var changed: Boolean = false

    /** Number contained in the cell on the board */
    var value: Int? = null

    /** Other nodes in the same row as this node. Does not contain this node */
    val row: Iterable<SudokuNode>
        get() = this._row

    /** Other nodes in the same column as this node. Does not contain this node */
    val column: Iterable<SudokuNode>
        get() = this._column

    /** Other nodes in the same box as this node. Does not contain this node */
    val box: Iterable<SudokuNode>
        get() = this._box

    /** Other nodes in the same box as this node. Does not contain this node. Empty if Hyper Sudoku is not being played */
    val hyper: Iterable<SudokuNode>
        get() = this._hyper

    /** Other nodes in the same neighborhood as this node. Does not contain this node */
    val all: Iterable<SudokuNode>
        get() {
            if (this.changed) {
                this._all = this._row union this._column union this._box union this._hyper
                this.changed = false
            }

            return this._all
        }

    /**
     * Inserts [other] into the row of this node.
     * Throws an exception if [other] corresponds to the same node as this node
     */
    fun addToRow(other: SudokuNode): Boolean {
        checkIfThis(other)

        this.changed = this._row.add(other)

        return this.changed
    }

    /**
     * Inserts [other] into the column of this node.
     * Throws an exception if [other] corresponds to the same node as this node
     */
    fun addToColumn(other: SudokuNode): Boolean {
        checkIfThis(other)

        this.changed = this._column.add(other)

        return this.changed
    }

    /**
     * Inserts [other] into the box of this node.
     * Throws an exception if [other] corresponds to the same node as this node
     */
    fun addToBox(other: SudokuNode): Boolean {
        checkIfThis(other)

        this.changed = this._box.add(other)

        return this.changed
    }

    /**
     * Inserts [other] into the hyper box of this node.
     * Throws an exception if [other] corresponds to the same node as this node
     */
    fun addToHyper(other: SudokuNode): Boolean {
        checkIfThis(other)

        this.changed = this._hyper.add(other)

        return this.changed
    }

    /** Throws an exception if this node is the same as [other] */
    private fun checkIfThis(other: SudokuNode) {
        if (this === other) {
            throw IllegalArgumentException("A node cannot have a connection to itself")
        }
    }
}

@Suppress("UNUSED")
@Serializable
class Cage(
    /** Target sum for this cage */
    val sum: Int,

    /** Set of coordinate positions for this cage */
    val positions: MutableSet<Position>
)

@Suppress("UNUSED")
@Serializable
class Box(
    /** Indicates if this box is a hyper box */
    val isHyper: Boolean,

    /** Set of coordinate positions for this box */
    val positions: MutableSet<Position>
)

@Suppress("UNUSED")
@Serializable
class SudokuJson(
    /** Current state of the Sudoku board */
    val board: List<List<Int?>>,

    /** Solved state of the Sudoku board */
    val solved: List<List<Int>>,

    /** Cages for this Sudoku board. Null if Killer Sudoku is not being played */
    val cages: Set<Cage>?,

    /** Set of boxes and hyper boxes for this Sudoku board */
    val boxes: Set<Box>,

    /** Number of rows/columns */
    val length: Int,

    /** Chosen difficulty for gameplay */
    val difficulty: Difficulty,

    /** Chosen game modifiers for gameplay */
    val games: Set<Game>
)

/** Generates a Sudoku board from scratch for gameplay */
fun makeSudoku(info: MakeSudokuCommand): SudokuJson {
    val length = info.dimension.length
    val difficulty = info.difficulty
    val games = info.games

    // Build table representing the sudoku and connect each cell in a graph
    val (neighborhoods, boxes) = buildBoard(info)

    // Fill entire table with values
    initializeValues(neighborhoods, info)

    // Generate cages if killer sudoku is being played
    val cages = makeCages(neighborhoods, info)

    // Save solved state of the sudoku for solution checking
    val solved = neighborhoods.map{ it.value!! }.unflatten(length)

    // Remove values from the sudoku in such a way that ensures there is only one solution
    adjustForDifficulty(neighborhoods, info)

    // Save unsolved state of the sudoku for playing
    val board = neighborhoods.map{ it.value }.unflatten(length)

    // Save all of the above information as JSON
    return SudokuJson(board, solved, cages, boxes, length, difficulty, games)
}
