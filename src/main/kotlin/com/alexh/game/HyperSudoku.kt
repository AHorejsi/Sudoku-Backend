package com.alexh.game

import com.alexh.utils.*
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
class HyperBox(
    val topLeft: Position,
    val bottomRight: Position,
    val overlapping: Boolean,
    val id: Int
)

enum class HyperDimension(
    val length: Int,
    val boxRows: Int,
    val boxCols: Int,
    val boxes: Set<HyperBox>
) {
    NINE(9, 3, 3, makeHyperBoxes(9, 3, 3)),
    SIXTEEN(16, 4, 4, makeHyperBoxes(16, 4, 4))
}

private fun makeHyperBoxes(length: Int, boxRows: Int, boxCols: Int): Set<HyperBox> {
    val boxes = mutableSetOf<HyperBox>()
    val endRow = length - boxRows
    val endCol = length - boxCols
    var index = 0

    for (rowIndex in 0 until length step boxRows) {
        for (colIndex in 0 until length step boxCols) {
            var topLeft = Position(rowIndex, colIndex)
            var bottomRight = Position(rowIndex + boxRows - 1, colIndex + boxCols - 1)
            var hyperBox = HyperBox(topLeft, bottomRight, false, index)

            boxes.add(hyperBox)
            ++index

            if (rowIndex != endRow && colIndex != endCol) {
                val rowIndex1 = (rowIndex + boxRows) / 2
                val colIndex1 = (colIndex + boxCols) / 2
                val rowIndex2 = rowIndex1 + boxRows
                val colIndex2 = colIndex1 + boxCols

                topLeft = Position(rowIndex1, colIndex1)
                bottomRight = Position(rowIndex2, colIndex2)
                hyperBox = HyperBox(topLeft, bottomRight, true, index)

                boxes.add(hyperBox)
                ++index
            }
        }
    }

    return boxes
}

enum class HyperDifficulty(
    val lowerBoundOfInitialGivens: Int,
    val upperBoundOfInitialGivens: Int,
    val lowerBoundOfInitialGivensPerUnit: Int
) {
    BEGINNER(68, 75, 66),
    EASY(57, 67, 44),
    MEDIUM(43, 56, 33),
    HARD(37, 40, 22),
    MASTER(27, 36, 11)
}

class HyperInfo(
    val dimension: HyperDimension,
    val difficulty: HyperDifficulty
)

private class HyperSafety(length: Int, boxCount: Int) {
    private val rowSafety: MutableList<Int>
    private val colSafety: MutableList<Int>
    private val boxSafety: MutableList<Int>

    init {
        val bits = (0.inv() shl length).inv() shl 1

        this.rowSafety = MutableList(length) { bits }
        this.colSafety = MutableList(length) { bits }
        this.boxSafety = MutableList(boxCount) { bits }
    }

    fun isSafe(rowIndex: Int, colIndex: Int, boxIndex1: Int, boxIndex2: Int?, value: Int): Boolean {
        val mask = 1 shl value

        val rowSafe = 0 != this.rowSafety[rowIndex] and mask
        val colSafe = 0 != this.colSafety[colIndex] and mask
        val box1Safe = 0 != this.boxSafety[boxIndex1] and mask
        val box2Safe = boxIndex2?.let { 0 != this.boxSafety[it] and mask } ?: true

        return rowSafe && colSafe && box1Safe && box2Safe
    }

    fun setSafe(rowIndex: Int, colIndex: Int, box1Id: Int, box2Id: Int?, value: Int) {
        val mask  = 1 shl value

        this.rowSafety[rowIndex] = this.rowSafety[rowIndex] or mask
        this.colSafety[colIndex] = this.colSafety[colIndex] or mask
        this.boxSafety[box1Id] = this.boxSafety[box1Id] or mask
        box2Id?.let { this.boxSafety[it] = this.boxSafety[it] or mask }
    }

    fun setUnsafe(rowIndex: Int, colIndex: Int, box1Id: Int, box2Id: Int?, value: Int) {
        val mask = (1 shl value).inv()

        this.rowSafety[rowIndex] = this.rowSafety[rowIndex] and mask
        this.colSafety[colIndex] = this.colSafety[colIndex] and mask
        this.boxSafety[box1Id] = this.boxSafety[box1Id] and mask
        box2Id?.let { this.boxSafety[it] = this.boxSafety[it] and mask }
    }

    fun weight(rowIndex: Int, colIndex: Int, box1Id: Int, box2Id: Int?): Quad<Int, Int, Int, Int?> {
        val rowWeight = this.rowSafety[rowIndex].countOneBits()
        val colWeight = this.colSafety[colIndex].countOneBits()
        val box1Weight = this.boxSafety[box1Id].countOneBits()
        val box2Weight = box2Id?.let { this.boxSafety[it].countOneBits() }

        return rowWeight to colWeight to box1Weight to box2Weight
    }
}

class HyperSudoku private constructor(
    internal val length: Int,
    internal val boxRows: Int,
    internal val boxCols: Int,
    private val boxes: Set<HyperBox>,
    private val difficulty: String,
    internal val lowerBoundOfInitialGivens: Int,
    internal val upperBoundOfInitialGivens: Int,
    internal val lowerBoundOfInitialGivensPerUnit: Int
) {
    companion object {
        fun make(info: HyperInfo, rand: Random): HyperJson {
            val dimension = info.dimension
            val difficulty = info.difficulty

            val puzzle = HyperSudoku(
                dimension.length,
                dimension.boxRows,
                dimension.boxCols,
                dimension.boxes,
                difficulty.name,
                difficulty.lowerBoundOfInitialGivens,
                difficulty.upperBoundOfInitialGivens,
                difficulty.lowerBoundOfInitialGivensPerUnit
            )

            initialValuesForHyper(puzzle, rand)
            //adjustForDifficultyForHyper(puzzle)
            //shuffleBoardOfHyper(puzzle)

            return puzzle.toJson()
        }
    }

    private val table: Array<Int?> = arrayOfNulls(this.length * this.length)
    private val safety: HyperSafety = HyperSafety(this.length, this.boxes.size)

    val legal: List<Int> = (1 .. this.length).toList()

    fun isSafe(rowIndex: Int, colIndex: Int, value: Int): Boolean {
        checkBounds(rowIndex, colIndex, this.length, this.length)
        checkLegal(value, this.length)

        val (boxIndex1, boxIndex2) = this.boxIds(rowIndex, colIndex)

        return this.safety.isSafe(rowIndex, colIndex, boxIndex1, boxIndex2, value)
    }

    fun getValue(rowIndex: Int, colIndex: Int): Int? {
        checkBounds(rowIndex, colIndex, this.length, this.length)

        return this.table[actualIndex(rowIndex, colIndex, this.length)]
    }

    fun setValue(rowIndex: Int, colIndex: Int, newValue: Int?) {
        checkBounds(rowIndex, colIndex, this.length, this.length)
        checkLegal(newValue, this.length)

        val index = actualIndex(rowIndex, colIndex, this.length)

        val oldValue = this.table[index]
        this.table[index] = newValue

        val (box1Id, box2Id) = this.boxIds(rowIndex, colIndex)

        if (null != oldValue) {
            this.safety.setSafe(rowIndex, colIndex, box1Id, box2Id, oldValue)
        }
        if (null != newValue) {
            this.safety.setUnsafe(rowIndex, colIndex, box1Id, box2Id, newValue)
        }
    }

    private fun boxIds(rowIndex: Int, colIndex: Int): Pair<Int, Int?> {
        var box1Id = -1
        var box2Id: Int? = null

        for (box in this.boxes) {
            val topLeft = box.topLeft
            val bottomRight = box.bottomRight

            if (
                topLeft.rowIndex <= rowIndex && topLeft.colIndex <= colIndex &&
                bottomRight.rowIndex >= rowIndex && bottomRight.colIndex >= colIndex
            ) {
                if (box.overlapping) {
                    box2Id = box.id
                }
                else {
                    box1Id = box.id
                }

                if (-1 != box1Id && null !== box2Id) {
                    break
                }
            }
        }

        return box1Id to box2Id
    }

    fun deleteValue(rowIndex: Int, colIndex: Int) = this.setValue(rowIndex, colIndex, null)

    private fun toJson(): HyperJson =
        HyperJson(this.table, this.length, this.boxes, this.difficulty)
}

@Serializable
class HyperJson(
    val table: Array<Int?>,
    val length: Int,
    val boxes: Set<HyperBox>,
    val difficulty: String
) {
    val kind: SudokuGame = SudokuGame.HYPER

    val solved: Boolean
        get() {
            TODO()
        }
}
