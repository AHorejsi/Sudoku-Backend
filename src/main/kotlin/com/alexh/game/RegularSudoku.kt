package com.alexh.game

import com.alexh.utils.actualIndex
import com.alexh.utils.boxIndex
import com.alexh.utils.to
import kotlinx.serialization.Serializable

enum class RegularDimension(
    val length: Int,
    val boxRows: Int,
    val boxCols: Int,
    val legal: List<Int>
) {
    NINE(9, 3, 3, (1 .. 9).toList()),
    TEN(10, 2, 5, (1 .. 10).toList()),
    TWELVE(12, 3, 4, (1 .. 12).toList()),
    FOURTEEN(14, 7, 2, (1 .. 14).toList()),
    FIFTEEN(15, 5, 3, (1 .. 15).toList()),
    SIXTEEN(16, 4, 4, (1 .. 16).toList()),
    EIGHTEEN(18, 3, 6, (1 .. 18).toList()),
    TWENTY(20, 4, 5, (1 .. 20).toList()),
    TWENTY_TWO(22, 2, 11, (1 .. 22).toList()),
    TWENTY_FOUR(24, 6, 4, (1 .. 24).toList()),
    TWENTY_FIVE(25, 5, 5, (1 .. 25).toList())
}

enum class RegularDifficulty(
    val title: String,
    val lowerBoundOfInitialGivens: Int,
    val upperBoundOfInitialGivens: Int,
    val lowerBoundOfInitialGivensPerUnit: Int
) {
    BEGINNER("Beginner", 58, 68, 55),
    EASY("Easy", 44, 57, 44),
    MEDIUM("Medium", 40, 43, 33),
    HARD("Hard", 35, 38, 22),
    MASTER("Master", 21, 33, 0)
}

class RegularInfo(
    val dimension: RegularDimension,
    val difficulty: RegularDifficulty
)

private class RegularSafety(length: Int) {
    private val rowSafety: MutableList<Int>
    private val colSafety: MutableList<Int>
    private val boxSafety: MutableList<Int>

    init {
        val bits = (0.inv() shl length).inv() shl 1

        this.rowSafety = MutableList(length) { bits }
        this.colSafety = MutableList(length) { bits }
        this.boxSafety = MutableList(length) { bits }
    }

    fun isSafe(rowIndex: Int, colIndex: Int, boxIndex: Int, value: Int): Boolean {
        val mask = 1 shl value

        val rowSafe = 0 != this.rowSafety[rowIndex] and mask
        val colSafe = 0 != this.colSafety[colIndex] and mask
        val boxSafe = 0 != this.boxSafety[boxIndex] and mask

        return rowSafe && colSafe && boxSafe
    }

    fun setSafe(rowIndex: Int, colIndex: Int, boxIndex: Int, value: Int) {
        val mask  = 1 shl value

        this.rowSafety[rowIndex] = this.rowSafety[rowIndex] or mask
        this.colSafety[colIndex] = this.colSafety[colIndex] or mask
        this.boxSafety[boxIndex] = this.boxSafety[boxIndex] or mask
    }

    fun setUnsafe(rowIndex: Int, colIndex: Int, boxIndex: Int, value: Int) {
        val mask = (1 shl value).inv()

        this.rowSafety[rowIndex] = this.rowSafety[rowIndex] and mask
        this.colSafety[colIndex] = this.colSafety[colIndex] and mask
        this.boxSafety[boxIndex] = this.boxSafety[boxIndex] and mask
    }

    fun weight(rowIndex: Int, colIndex: Int, boxIndex: Int): Triple<Int, Int, Int> {
        val rowWeight = this.rowSafety[rowIndex].countOneBits()
        val colWeight = this.colSafety[colIndex].countOneBits()
        val boxWeight = this.boxSafety[boxIndex].countOneBits()

        return rowWeight to colWeight to boxWeight
    }
}

class RegularSudoku internal constructor(
    val length: Int,
    val boxRows: Int,
    val boxCols: Int,
    internal val legal: List<Int>,
    private val difficulty: String,
    internal val lowerBoundOfInitialGivens: Int,
    internal val upperBoundOfInitialGivens: Int,
    internal val lowerBoundOfInitialGivensPerUnit: Int
) {
    companion object {
        fun make(info: RegularInfo): RegularJson {
            val dimension = info.dimension
            val difficulty = info.difficulty

            val puzzle = RegularSudoku(
                dimension.length,
                dimension.boxRows,
                dimension.boxCols,
                dimension.legal,
                difficulty.title,
                difficulty.lowerBoundOfInitialGivens,
                difficulty.upperBoundOfInitialGivens,
                difficulty.lowerBoundOfInitialGivensPerUnit
            )

            initializeValuesForRegular(puzzle)
            adjustForDifficultyForRegular(puzzle)
            shuffleBoardOfRegular(puzzle)

            return puzzle.toJson()
        }
    }

    private val table: Array<Int?> = Array(this.length * this.length) { null }
    private val safety: RegularSafety = RegularSafety(this.length)

    fun isSafe(rowIndex: Int, colIndex: Int, value: Int): Boolean {
        this.checkLegal(value)
        this.checkBounds(rowIndex, colIndex)

        val boxIndex = boxIndex(rowIndex, colIndex, this.boxRows, this.boxCols)

        return this.safety.isSafe(rowIndex, colIndex, boxIndex, value)
    }

    private fun setUnsafe(rowIndex: Int, colIndex: Int, value: Int) {
        this.checkBounds(rowIndex, colIndex)

        val boxIndex = boxIndex(rowIndex, colIndex, this.boxRows, this.boxCols)

        return this.safety.setUnsafe(rowIndex, colIndex, boxIndex, value)
    }

    private fun setSafe(rowIndex: Int, colIndex: Int, value: Int) {
        this.checkBounds(rowIndex, colIndex)

        val boxIndex = boxIndex(rowIndex, colIndex, this.boxRows, this.boxCols)

        return this.safety.setSafe(rowIndex, colIndex, boxIndex, value)
    }

    internal fun givens(rowIndex: Int, colIndex: Int): Triple<Int, Int, Int> {
        val boxIndex = boxIndex(rowIndex, colIndex, this.boxRows, this.boxCols)
        val (rowEmptyCount, colEmptyCount, boxEmptyCount) = this.safety.weight(rowIndex, colIndex, boxIndex)

        return (this.length - rowEmptyCount) to (this.length - colEmptyCount) to (this.length - boxEmptyCount)
    }

    fun getValue(rowIndex: Int, colIndex: Int): Int? = this.table[actualIndex(rowIndex, colIndex, this.length)]

    fun setValue(rowIndex: Int, colIndex: Int, newValue: Int?) {
        this.checkLegal(newValue)

        val index = actualIndex(rowIndex, colIndex, this.length)

        val oldValue = this.table[index]
        this.table[index] = newValue

        if (null !== oldValue) {
            this.setSafe(rowIndex, colIndex, oldValue)
        }
        if (null !== newValue) {
            this.setUnsafe(rowIndex, colIndex, newValue)
        }
    }

    fun deleteValue(rowIndex: Int, colIndex: Int) = this.setValue(rowIndex, colIndex, null)

    private fun checkLegal(value: Int?) {
        if (!this.isLegal(value)) {
            throw IllegalArgumentException("Illegal value: $value")
        }
    }

    private fun isLegal(value: Int?): Boolean = null === value || value in this.legal

    private fun checkBounds(rowIndex: Int, colIndex: Int) {
        if (rowIndex < 0 || rowIndex >= this.length || colIndex < 0 || colIndex >= this.length) {
            throw IndexOutOfBoundsException("Indices out of bounds: (Row Index = $rowIndex), (Column Index = $colIndex), (Length = ${this.length})")
        }
    }

    private fun toJson(): RegularJson =
        RegularJson(this.table, this.length, this.boxRows, this.boxCols, this.difficulty)
}

@Serializable
class RegularJson(
    val table: Array<Int?>,
    val length: Int,
    val boxRows: Int,
    val boxCols: Int,
    val difficulty: String
)
