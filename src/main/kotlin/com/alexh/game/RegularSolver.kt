package com.alexh.game

private class ExactCoverNode(
    column: ExactCoverNode?=null
) {
    var up: ExactCoverNode = this
    var down: ExactCoverNode = this
    var left: ExactCoverNode = this
    var right: ExactCoverNode = this
    var column: ExactCoverNode = column ?: this
    var size: Int = 0

    fun hookDown(other: ExactCoverNode) {
        other.down = this.down
        other.down.up = other
        other.up = this
        this.down = other
    }

    fun hookRight(other: ExactCoverNode) {
        other.right = this.right
        other.right.left = other
        other.left = this
        this.right = other
    }

    fun unlinkLeftRight() {
        this.right.left = this.left
        this.left.right = this.right
    }

    fun relinkLeftRight() {
        this.left.right = this
        this.right.left = this
    }

    fun unlinkUpDown() {
        this.up.down = this.down
        this.down.up = this.up
    }

    fun relinkUpDown() {
        this.down.up = this
        this.up.down = this
    }

    fun cover() {
        if (this !== this.column) {
            throw IllegalStateException("This must be a column node")
        }

        this.unlinkLeftRight()

        var node1 = this.down

        while (this !== node1) {
            var node2 = node1.right

            while (node1 !== node2) {
                node2.unlinkUpDown()
                --(node2.column.size)

                node2 = node2.right
            }

            node1 = node1.down
        }
    }

    fun uncover() {
        if (this !== this.column) {
            throw IllegalStateException("This must be a column node")
        }

        var node1 = this.up

        while (this !== node1) {
            var node2 = node1.left

            while (node1 !== node2) {
                ++(node2.column.size)
                node2.relinkUpDown()

                node2 = node2.left
            }

            node1 = node1.up
        }

        this.relinkLeftRight()
    }
}

internal fun hasUniqueSolution(puzzle: RegularSudoku): Boolean {
    val matrix = makeMatrix(puzzle)
    val header = makeDoublyLinkedMatrix(matrix)

    val solutionCount = countSolutions(0, header)

    return 1 == solutionCount
}

private fun makeMatrix(puzzle: RegularSudoku): Array<Array<Boolean>> {
    val length = puzzle.length
    val range = 0 until length
    val matrix = initializeMatrix(length)
    var hBase = 0

    hBase = checkCellConstraint(hBase, matrix, range, length)
    hBase = checkRowConstraint(hBase, matrix, range, length)
    hBase = checkColConstraint(hBase, matrix, range, length)
    checkBoxConstraint(hBase, matrix, range, length, puzzle.boxRows, puzzle.boxCols)

    placeInitialValues(matrix, puzzle, range, length)

    return matrix
}

private fun initializeMatrix(length: Int): Array<Array<Boolean>> {
    val rowCount = length * length * length
    val colCount = 4 * length * length
    val matrix = Array(rowCount) { Array(colCount) { false } }

    return matrix
}

private fun checkCellConstraint(hBase: Int, matrix: Array<Array<Boolean>>, range: IntRange, length: Int): Int {
    var hBaseVal = hBase

    for (rowIndex in range) {
        for (colIndex in range) {
            for (valueIndex in range) {
                val index = index(rowIndex, colIndex, valueIndex, length)

                matrix[index][hBaseVal] = true
            }

            ++hBaseVal
        }
    }

    return hBaseVal
}

private fun checkRowConstraint(hBase: Int, matrix: Array<Array<Boolean>>, range: IntRange, length: Int): Int {
    var hBaseVal = hBase

    for (rowIndex in range) {
        for (valueIndex in range) {
            for (colIndex in range) {
                val index = index(rowIndex, colIndex, valueIndex, length)

                matrix[index][hBaseVal] = true
            }

            ++hBaseVal
        }
    }

    return hBaseVal
}

private fun checkColConstraint(hBase: Int, matrix: Array<Array<Boolean>>, range: IntRange, length: Int): Int {
    var hBaseVal = hBase

    for (colIndex in range) {
        for (valueIndex in range) {
            for (rowIndex in range) {
                val index = index(rowIndex, colIndex, valueIndex, length)

                matrix[index][hBaseVal] = true
            }

            ++hBaseVal
        }
    }

    return hBaseVal
}

private fun checkBoxConstraint(
    hBase: Int,
    matrix: Array<Array<Boolean>>,
    range: IntRange,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    var hBaseVal = hBase
    val rowRange = range step boxRows
    val colRange = range step boxCols

    for (rowIndex in rowRange) {
        for (colIndex in colRange) {
            for (valueIndex in range) {
                traverseBox(hBaseVal, matrix, rowIndex, colIndex, valueIndex, length, boxRows, boxCols)

                ++hBaseVal
            }
        }
    }
}

private fun traverseBox(
    hBaseVal: Int,
    matrix: Array<Array<Boolean>>,
    rowIndex: Int,
    colIndex: Int,
    valueIndex: Int,
    length: Int,
    boxRows: Int,
    boxCols: Int
) {
    val rowRange = 0 until boxRows
    val colRange = 0 until boxCols

    for (rowDelta in rowRange) {
        for (colDelta in colRange) {
            val index = index(rowIndex + rowDelta, colIndex + colDelta, valueIndex, length)

            matrix[index][hBaseVal] = true
        }
    }
}

private fun placeInitialValues(matrix: Array<Array<Boolean>>, puzzle: RegularSudoku, range: IntRange, length: Int) {
    val legal = puzzle.legal

    for (rowIndex in range) {
        for (colIndex in range) {
            puzzle.getValue(rowIndex, colIndex)?.let {
                placeValue(it, rowIndex, colIndex, range, length, matrix, legal)
            }
        }
    }
}

private fun placeValue(
    value: Int,
    rowIndex: Int,
    colIndex: Int,
    range: IntRange,
    length: Int,
    matrix: Array<Array<Boolean>>,
    legal: List<Int>
) {
    for (valueIndex in range) {
        if (value != legal[valueIndex]) {
            val index = index(rowIndex, colIndex, valueIndex, length)

            matrix[index].fill(false)
        }
    }
}

private fun index(rowIndex: Int, colIndex: Int, valueIndex: Int, length: Int): Int =
    rowIndex * length * length + colIndex * length + valueIndex

private fun makeDoublyLinkedMatrix(matrix: Array<Array<Boolean>>): ExactCoverNode {
    val headers = mutableListOf<ExactCoverNode>()
    val cols = matrix[0].size

    val mainHead = makeHeaders(headers, cols)
    initializeDoublyLinkedMatrix(mainHead, headers, cols, matrix)

    return mainHead
}

private fun makeHeaders(headers: MutableList<ExactCoverNode>, cols: Int): ExactCoverNode {
    var mainHead = ExactCoverNode()

    for (count in 0 until cols) {
        val headNode = ExactCoverNode()

        headers.add(headNode)

        mainHead.hookRight(headNode)

        mainHead = headNode
    }

    return mainHead.right.column
}

private fun initializeDoublyLinkedMatrix(
    mainHead: ExactCoverNode,
    headers: MutableList<ExactCoverNode>,
    cols: Int,
    matrix: Array<Array<Boolean>>
) {
    for (row in matrix) {
        var prev: ExactCoverNode? = null

        for (col in 0 until cols) {
            if (row[col]) {
                val headNode = headers[col]
                val newNode = ExactCoverNode(headNode)

                if (null === prev) {
                    prev = newNode
                }

                headNode.up.hookDown(newNode)
                prev.hookRight(newNode)

                prev = newNode

                ++(headNode.size)
            }
        }
    }

    mainHead.size = cols
}

private fun countSolutions(count: Int, header: ExactCoverNode): Int {
    if (header.right === header) {
        return count + 1
    }

    var countVal = count
    var colNode = chooseNextColumn(header)

    colNode.cover()

    var node1 = colNode.down

    while (node1 !== colNode) {
        var node2 = node1.right

        while (node1 !== node2) {
            node2.column.cover()

            node2 = node2.right
        }

        countVal = countSolutions(countVal, header)

        if (countVal > 1) {
            return countVal
        }

        colNode = node1.column

        node2 = node1.left

        while (node1 !== node2) {
            node2.column.uncover()

            node2 = node2.left
        }

        node1 = node1.down
    }

    colNode.uncover()

    return countVal
}

private fun chooseNextColumn(header: ExactCoverNode): ExactCoverNode {
    var minimum = Int.MAX_VALUE
    var nextToUse = header
    var colNode = header.right

    while (colNode !== header) {
        val size = colNode.size

        if (size < minimum) {
            minimum = size
            nextToUse = colNode
        }

        colNode = colNode.right
    }

    return nextToUse
}
