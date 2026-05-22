package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.get2d
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

enum class Game { HYPER, KILLER }

enum class Dimension(
    val length: Int,
    val boxRows: Int,
    val boxCols: Int
) {
    NINE(9, 3, 3)
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
    MASTER(0.21f, 0.33f, 0.0f, 0.77f, 0.88f)
}

@Serializable
class MakeSudokuCommand(
    val dimension: Dimension,
    val difficulty: Difficulty,
    val games: Set<Game>,
    @Transient val random: Random = Random.Default
)

internal class SudokuNode(val place: Position) {
    private val _row = hashSetOf<SudokuNode>()
    private val _column = hashSetOf<SudokuNode>()
    private val _box = hashSetOf<SudokuNode>()
    private val _hyper = hashSetOf<SudokuNode>()
    private var _all = emptySet<SudokuNode>()

    private var changed = false
    var value: Int? = null

    val row: Set<SudokuNode>
        get() = this._row

    val column: Set<SudokuNode>
        get() = this._column

    val box: Set<SudokuNode>
        get() = this._box

    val hyper: Set<SudokuNode>
        get() = this._hyper

    val all: Set<SudokuNode>
        get() {
            if (this.changed) {
                this._all = this._row union this._column union this._box union this._hyper
                this.changed = false
            }

            return this._all
        }

    fun addToRow(other: SudokuNode): Boolean =
        this.insertNode(other, this._row)

    fun addToColumn(other: SudokuNode): Boolean =
        this.insertNode(other, this._column)

    fun addToBox(other: SudokuNode): Boolean =
        this.insertNode(other, this._box)

    fun addToHyper(other: SudokuNode): Boolean =
        this.insertNode(other, this._hyper)

    private fun insertNode(other: SudokuNode, nodeSet: MutableSet<SudokuNode>): Boolean {
        require(this !== other) { "A SudokuNode cannot be connected to itself" }

        this.changed = nodeSet.add(other)

        return this.changed
    }
}

internal class SudokuGraph(
    private val neighborhoods: List<SudokuNode>,
    val length: Int
) : Iterable<SudokuNode> {
    val size: Int
        get() = this.neighborhoods.size

    fun get(rowIndex: Int, colIndex: Int): SudokuNode =
        this.neighborhoods.get2d(rowIndex, colIndex, this.length)

    fun get(pos: Position): SudokuNode =
        this.get(pos.rowIndex, pos.colIndex)

    fun set(rowIndex: Int, colIndex: Int, value: Int) {
        val node = this.get(rowIndex, colIndex)

        node.value = value
    }

    fun <TType> save(converter: (SudokuNode) -> TType): List<List<TType>> {
        val table = ArrayList<List<TType>>(this.length)
        val nodeIter = this.iterator()

        while (nodeIter.hasNext()) {
            val row = ArrayList<TType>(this.length)

            repeat(this.length) { _ ->
                val node = nodeIter.next()
                val item = converter(node)

                row.add(item)
            }

            table.add(row)
        }

        return table
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
    val notes = 0
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
    val graph = buildBoard(info.dimension, info.games)

    // Create boxes within the larger puzzle
    val boxes = createBoxSet(info.dimension, info.games)

    // Fill entire table with values
    initializeValues(graph, info.dimension, info.games, info.random)

    // Shuffle values around in such a way that the chosen rules are still adhered to
    shuffleValues(graph, info.dimension, info.games, info.random)

    // Save solved state of the sudoku for solution checking
    val solved = graph.save{ it.value!! }

    // Generate cages if killer sudoku is being played
    val cages = makeCages(graph, info)

    // Remove values from the sudoku in such a way that ensures there is only one solution
    adjustForDifficulty(graph, info)

    // Save unsolved state of the sudoku for gameplay
    val board = graph.save{ Cell(it.value) }

    // Save all of the above information as JSON
    return SudokuJson(board, solved, cages, boxes, info)
}
