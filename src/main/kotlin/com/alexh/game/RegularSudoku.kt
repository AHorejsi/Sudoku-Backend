package com.alexh.game

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
        val rowWeight = this.hammingWeight(this.rowSafety[rowIndex])
        val colWeight = this.hammingWeight(this.colSafety[colIndex])
        val boxWeight = this.hammingWeight(this.boxSafety[boxIndex])

        return Triple(rowWeight, colWeight, boxWeight)
    }

    private fun hammingWeight(bits: Int): Int {
        var value = bits - ((bits shl 1) and 0x55555555)
        value = (value and 0x33333333) + ((value shr 2) and 0x33333333)
        value = (((value + (value shr 4)) and 0x0f0f0f0f) * 0x1010101) shr 24

        return value
    }
}

class RegularSudoku internal constructor(
    override val length: Int,
    override val boxRows: Int,
    override val boxCols: Int,
    internal val legal: List<Int>,
    override val difficulty: String,
    internal val lowerBoundOfInitialGivens: Int,
    internal val upperBoundOfInitialGivens: Int,
    internal val lowerBoundOfInitialGivensPerUnit: Int
) : SudokuPuzzle {
    companion object {
        fun make(info: RegularInfo): RegularSudoku {
            val puzzle = RegularSudoku(info)

            initializeValues(puzzle)
            adjustForDifficulty(puzzle)
            shuffleBoard(puzzle)

            puzzle.finishInitialization()

            return puzzle
        }
    }

    internal val rowBoxCount: Int = this.length / this.boxRows
    internal val colBoxCount: Int = this.length / this.boxCols
    private val table: List<Cell> = List(this.length) { Cell(this.length) }
    private val safety: RegularSafety = RegularSafety(this.length)

    internal constructor(info: RegularInfo) :
            this(info.dimension.length, info.dimension.boxRows, info.dimension.boxCols, info.dimension.legal,
                info.difficulty.title, info.difficulty.lowerBoundOfInitialGivens,
                info.difficulty.upperBoundOfInitialGivens, info.difficulty.lowerBoundOfInitialGivensPerUnit)

    override fun isLegal(value: Int?): Boolean = null === value || value in this.legal

    override fun isSafe(rowIndex: Int, colIndex: Int, value: Int): Boolean {
        this.checkBounds(rowIndex, colIndex)

        val boxIndex = this.boxIndex(rowIndex, colIndex)

        return this.safety.isSafe(rowIndex, colIndex, boxIndex, value)
    }

    private fun setUnsafe(rowIndex: Int, colIndex: Int, value: Int) {
        this.checkBounds(rowIndex, colIndex)

        val boxIndex = this.boxIndex(rowIndex, colIndex)

        return this.safety.setUnsafe(rowIndex, colIndex, boxIndex, value)
    }

    private fun setSafe(rowIndex: Int, colIndex: Int, value: Int) {
        this.checkBounds(rowIndex, colIndex)

        val boxIndex = this.boxIndex(rowIndex, colIndex)

        return this.safety.setSafe(rowIndex, colIndex, boxIndex, value)
    }

    internal fun givens(rowIndex: Int, colIndex: Int): Triple<Int, Int, Int> {
        val boxIndex = this.boxIndex(rowIndex, colIndex)
        val (rowEmptyCount, colEmptyCount, boxEmptyCount) = this.safety.weight(rowIndex, colIndex, boxIndex)

        return Triple(this.length - rowEmptyCount, this.length - colEmptyCount, this.length - boxEmptyCount)
    }

    private fun boxIndex(rowIndex: Int, colIndex: Int): Int = rowIndex / this.boxRows * this.boxRows + colIndex / this.boxCols

    override fun getValue(rowIndex: Int, colIndex: Int): Int? = this.getCell(rowIndex, colIndex).value

    override fun setValue(rowIndex: Int, colIndex: Int, newValue: Int?) {
        val cell = this.getCell(rowIndex, colIndex)

        val oldValue = cell.value
        cell.value = newValue

        if (null !== oldValue) {
            this.setSafe(rowIndex, colIndex, oldValue)
        }
        if (null !== newValue) {
            this.setUnsafe(rowIndex, colIndex, newValue)
        }
    }

    override fun getTentative(rowIndex: Int, colIndex: Int): TentativeList =
        this.getCell(rowIndex, colIndex).tentative

    private fun getCell(rowIndex: Int, colIndex: Int): Cell {
        this.checkBounds(rowIndex, colIndex)

        return this.table[rowIndex * this.length + colIndex]
    }

    private fun checkBounds(rowIndex: Int, colIndex: Int) {
        if (rowIndex < 0 || rowIndex >= this.length || colIndex < 0 || colIndex >= this.length) {
            throw IndexOutOfBoundsException("Indices out of bounds: (Row Index = $rowIndex), (Column Index = $colIndex)")
        }
    }

    override val complete: Boolean
        get() {
            for (cell in this.table) {
                if (null === cell.value) {
                    return false
                }
            }

            return true
        }

    override val valid: Boolean
        get() {
            val length = this.length

            val rowSeen = MutableList(length) { 0 }
            val colSeen = MutableList(length) { 0 }
            val boxSeen = MutableList(length) { 0 }

            for (rowIndex in 1 until length) {
                for (colIndex in 1 until length) {
                    val value = this.getValue(rowIndex, colIndex)

                    if (null !== value) {
                        val boxIndex = this.boxIndex(rowIndex, colIndex)
                        val mask = 1 shl value

                        if (0 != rowSeen[rowIndex] and mask)
                            return false
                        else
                            rowSeen[rowIndex] = rowSeen[rowIndex] or mask

                        if (0 != colSeen[colIndex] and mask)
                            return false
                        else
                            colSeen[colIndex] = colSeen[colIndex] or mask

                        if (0 != boxSeen[boxIndex] and mask)
                            return false
                        else
                            boxSeen[boxIndex] = boxSeen[boxIndex] or mask
                    }
                }
            }

            return true
        }

    private fun finishInitialization() {
        for (cell in this.table) {
            if (null === cell.value) {
                cell.editable = false
            }
        }
    }
}
