package com.alexh.game

import com.alexh.utils.*
import kotlinx.serialization.Serializable
import java.lang.IllegalArgumentException
import kotlin.random.Random

enum class RegularDimension(
    val length: Int,
    val boxRows: Int,
    val boxCols: Int
) {
    NINE(9, 3, 3),
    TEN(10, 2, 5),
    TWELVE(12, 3, 4),
    SIXTEEN(16, 4, 4),
    FIFTEEN(15, 5, 3),
    EIGHTEEN(18, 3, 6),
    TWENTY(20, 4, 5),
    TWENTY_TWO(22, 2, 11),
    TWENTY_FOUR(24, 6, 4),
    TWENTY_FIVE(25, 5, 5)
}

enum class RegularDifficulty(
    val lowerBoundOfInitialGivens: Int,
    val upperBoundOfInitialGivens: Int,
    val lowerBoundOfInitialGivensPerUnit: Int
) {
    BEGINNER(58, 68, 55),
    EASY(44, 57, 44),
    MEDIUM(40, 43, 33),
    HARD(34, 39, 22),
    MASTER(21, 33, 0)
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
    val difficulty: String,
    val lowerBoundOfInitialGivens: Int,
    val upperBoundOfInitialGivens: Int,
    val lowerBoundOfInitialGivensPerUnit: Int
) {
    companion object {
        fun make(info: RegularInfo, rand: Random): RegularSudoku {
            val dimension = info.dimension
            val difficulty = info.difficulty

            val puzzle = RegularSudoku(
                dimension.length,
                dimension.boxRows,
                dimension.boxCols,
                difficulty.name,
                difficulty.lowerBoundOfInitialGivens,
                difficulty.upperBoundOfInitialGivens,
                difficulty.lowerBoundOfInitialGivensPerUnit
            )

            initializeValuesForRegular(puzzle, rand)
            adjustForDifficultyForRegular(puzzle, rand)
            shuffleBoardOfRegular(puzzle, rand)

            return puzzle
        }
    }

    private val table: Array<Int?> = arrayOfNulls(this.length * this.length)
    private val complete: Array<Int> = Array(this.length * this.length) { -1 }
    private val safety: RegularSafety = RegularSafety(this.length)

    val solved: Boolean
        get() {
            for ((inputVal, correctVal) in this.table.zip(this.complete)) {
                if (inputVal != correctVal) {
                    return false
                }
            }

            return true
        }

    internal fun isSafe(rowIndex: Int, colIndex: Int, value: Int): Boolean {
        checkBounds(rowIndex, colIndex, this.length, this.length)
        checkLegal(value, this.length)

        val boxIndex = boxIndex(rowIndex, colIndex, this.boxRows, this.boxCols)

        return this.safety.isSafe(rowIndex, colIndex, boxIndex, value)
    }

    internal fun givens(rowIndex: Int, colIndex: Int): Triple<Int, Int, Int> {
        val boxIndex = boxIndex(rowIndex, colIndex, this.boxRows, this.boxCols)
        val (rowEmptyCount, colEmptyCount, boxEmptyCount) = this.safety.weight(rowIndex, colIndex, boxIndex)

        return (this.length - rowEmptyCount) to (this.length - colEmptyCount) to (this.length - boxEmptyCount)
    }

    internal fun getValue(rowIndex: Int, colIndex: Int): Int? {
        checkBounds(rowIndex, colIndex, this.length, this.length)

        return this.table[actualIndex(rowIndex, colIndex, this.length)]
    }

    internal fun setValue(rowIndex: Int, colIndex: Int, newValue: Int?) {
        checkBounds(rowIndex, colIndex, this.length, this.length)
        checkLegal(newValue, this.length)

        val index = actualIndex(rowIndex, colIndex, this.length)

        val oldValue = this.table[index]
        this.table[index] = newValue

        if (null !== newValue) {
            this.complete[index] = newValue
        }

        val boxIndex = boxIndex(rowIndex, colIndex, this.boxRows, this.boxCols)

        if (null !== oldValue) {
            this.safety.setSafe(rowIndex, colIndex, boxIndex, oldValue)
        }
        if (null !== newValue) {
            this.safety.setUnsafe(rowIndex, colIndex, boxIndex, newValue)
        }
    }

    internal fun deleteValue(rowIndex: Int, colIndex: Int) = this.setValue(rowIndex, colIndex, null)

    fun toJson(): RegularJson =
        RegularJson(this.table, this.complete, this.length, this.boxRows, this.boxCols, this.difficulty)
}

@Serializable
class RegularJson(
    private val table: Array<Int?>,
    private val complete: Array<Int>,
    private val length: Int,
    private val boxRows: Int,
    private val boxCols: Int,
    private val difficulty: String
) {
    private val kind: SudokuGame = SudokuGame.REGULAR
}
