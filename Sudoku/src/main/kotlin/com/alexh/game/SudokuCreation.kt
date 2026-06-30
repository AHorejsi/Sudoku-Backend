package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.get2d
import java.util.EnumSet
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.math.pow
import kotlin.random.Random

enum class Game {
    HYPER, KILLER;

    companion object {
        private val THREAD_SAFETY = LazyThreadSafetyMode.PUBLICATION

        val states: Set<Game> by lazy(THREAD_SAFETY) { EnumSet.allOf(Game::class.java) }
        val subsets: Set<Set<Game>> by lazy(THREAD_SAFETY) { Game.makeSubsets() }

        private fun makeSubsets(): Set<Set<Game>> {
            val subsetAmount = 2.0.pow(Game.states.size).toInt()

            val subsets = LinkedHashSet<Set<Game>>(subsetAmount)

            subsets.add(emptySet())
            subsets.add(EnumSet.of(HYPER))
            subsets.add(EnumSet.of(KILLER))
            subsets.add(EnumSet.of(HYPER, KILLER))

            return subsets
        }
    }
}

enum class Dimension(
    val length: Int,
    val boxRows: Int,
    val boxCols: Int
) {
    NINE(9, 3, 3);

    companion object {
        val states: Set<Dimension> =
            setOf(NINE)
    }
}

enum class Difficulty(
    val initialGivenLowerBound: Float,
    val initialGivenUpperBound: Float,
    val initialGivensPerNeighborhood: Float,
    val minCageSize: Float,
    val maxCageSize: Float
) {
    BEGINNER(0.58f, 0.68f, 0.55f, 0.33f, 0.44f),
    EASY(0.44f, 0.57f, 0.44f, 0.44f, 0.55f),
    MEDIUM(0.40f, 0.43f, 0.33f, 0.55f, 0.66f),
    HARD(0.35f, 0.38f, 0.22f, 0.66f, 0.77f),
    MASTER(0.21f, 0.33f, 0.0f, 0.77f, 0.88f);

    companion object {
        val states: Set<Difficulty> =
            EnumSet.allOf(Difficulty::class.java)
    }
}

@Serializable
class MakeSudokuCommand(
    val dimension: Dimension,
    val difficulty: Difficulty,
    val games: Set<Game>,
    @Transient val random: Random = Random.Default
) {
    override fun equals(other: Any?): Boolean {
        if (other !is MakeSudokuCommand) {
            return false
        }

        return this.dimension == other.dimension &&
                this.difficulty == other.difficulty &&
                this.games == other.games
    }

    override fun hashCode(): Int {
        var hashValue = 0
        hashValue += this.dimension.hashCode()
        hashValue += this.difficulty.hashCode()
        hashValue += this.games.hashCode()
        hashValue *= 31

        return hashValue
    }
}

internal class SudokuNode(val place: Position) {
    private val _row = hashSetOf<SudokuNode>()
    private val _column = hashSetOf<SudokuNode>()
    private val _box = hashSetOf<SudokuNode>()
    private val _hyper = hashSetOf<SudokuNode>()

    var value: Int? = null

    val row: Set<SudokuNode>
        get() = this._row

    val column: Set<SudokuNode>
        get() = this._column

    val box: Set<SudokuNode>
        get() = this._box

    val hyper: Set<SudokuNode>
        get() = this._hyper

    val all: Set<SudokuNode> by lazy(LazyThreadSafetyMode.NONE) {
        val unionSet = HashSet<SudokuNode>(4 * this._row.size)

        unionSet.addAll(this._row)
        unionSet.addAll(this._column)
        unionSet.addAll(this._box)
        unionSet.addAll(this._hyper)

        return@lazy unionSet
    }

    fun addToRow(other: SudokuNode): Boolean =
        this.insertNode(other, this._row)

    fun addToColumn(other: SudokuNode): Boolean =
        this.insertNode(other, this._column)

    fun addToBox(other: SudokuNode): Boolean =
        this.insertNode(other, this._box)

    fun addToHyper(other: SudokuNode): Boolean =
        this.insertNode(other, this._hyper)

    private fun insertNode(otherNode: SudokuNode, nodeSet: MutableSet<SudokuNode>): Boolean {
        if (this === otherNode) {
            throw IllegalArgumentException("A SudokuNode cannot be connected to itself")
        }

        return nodeSet.add(otherNode)
    }
}

internal class SudokuGraph(
    private val neighborhoods: List<SudokuNode>,
    val length: Int
) : Iterable<SudokuNode> {
    val size: Int
        get() = this.neighborhoods.size

    operator fun get(rowIndex: Int, colIndex: Int): SudokuNode =
        this.neighborhoods.get2d(rowIndex, colIndex, this.length)

    operator fun get(pos: Position): SudokuNode =
        this[pos.rowIndex, pos.colIndex]

    operator fun set(rowIndex: Int, colIndex: Int, value: Int) {
        val node = this[rowIndex, colIndex]

        node.value = value
    }

    operator fun set(pos: Position, value: Int) {
        this[pos.rowIndex, pos.colIndex] = value
    }

    fun saveInSolvedState(): List<MutableList<Int>> {
        val iter = this.iterator()
        val table = ArrayList<MutableList<Int>>(this.length)

        repeat(this.length) {
            val row = this.makeSolvedRow(iter)

            table.add(row)
        }

        return table
    }

    private fun makeSolvedRow(iter: Iterator<SudokuNode>): MutableList<Int> {
        val row = ArrayList<Int>(this.length)

        repeat(this.length) { _ ->
            val node = iter.next()

            node.value?.let {
                row.add(it)
            } ?: run {
                throw IllegalStateException("Incomplete Sudoku")
            }
        }

        return row
    }

    fun saveInUnsolvedState(): List<List<Cell>> {
        val iter = this.iterator()
        val cells = ArrayList<List<Cell>>(this.length)

        repeat(this.length) { _ ->
            val row = this.makeUnsolvedRow(iter)

            cells.add(row)
        }

        return cells
    }

    private fun makeUnsolvedRow(iter: Iterator<SudokuNode>): List<Cell> {
        val row = ArrayList<Cell>(this.length)

        repeat(this.length) { _ ->
            val node = iter.next()
            val cell = Cell(node.value)

            row.add(cell)
        }

        return row
    }

    override fun iterator(): Iterator<SudokuNode> =
        this.neighborhoods.iterator()
}

@Serializable
class Cage internal constructor(
    val sum: Int,
    val positions: Set<Position>
)

@Serializable
class Box internal constructor(
    val isHyper: Boolean,
    val positions: Set<Position>
)

@Serializable
class Cell internal constructor(val value: Int?) {
    val notes = mutableListOf<Int>()
    val editable = null === this.value
}

@Serializable
class SudokuJson internal constructor(
    val board: List<List<Cell>>,
    val solved: List<List<Int>>,
    val cages: Set<Cage>?,
    val boxes: Set<Box>,
    val description: MakeSudokuCommand
)

fun makeSudoku(info: MakeSudokuCommand): SudokuJson {
    // Build table representing the sudoku and connect each cell in a graph
    val graph = buildGraph(info.dimension, info.games, info.random)

    // Create boxes within the larger puzzle
    val boxes = constructBoxSet(graph, info.games)

    // Fill entire table with values
    initializeValues(graph, info.dimension, info.games, info.random)

    // Save solved state of the sudoku for solution checking
    val solved = graph.saveInSolvedState()

    // Shuffle values around in such a way that the chosen rules are still adhered to
    shuffleBoard(graph, solved, info.dimension, info.games, info.random)

    // Remove values from the sudoku in such a way that ensures there is only one solution
    adjustForDifficulty(graph, info)

    // Shuffle values around in such a way that the chosen rules are still adhered to
    shuffleBoard(graph, solved, info.dimension, info.games, info.random)

    // Generate cages if killer sudoku is being played
    val cages = formCages(solved, info)

    // Save unsolved state of the sudoku for gameplay
    val board = graph.saveInUnsolvedState()

    // Save all of the above information as JSON
    return SudokuJson(board, solved, cages, boxes, info)
}
