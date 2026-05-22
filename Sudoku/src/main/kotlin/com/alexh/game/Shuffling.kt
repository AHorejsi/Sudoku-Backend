package com.alexh.game

import com.alexh.utils.up
import kotlin.random.Random

internal fun shuffleValues(graph: SudokuGraph, dimension: Dimension, games: Set<Game>, rand: Random) {
    shuffleByFlipping(graph, dimension, rand)
    shuffleByTransposing(graph, dimension, rand)
    shuffleByRotating(graph, dimension, rand)
    shuffleBySwapping(graph, dimension, rand)
    //shuffleByBoxes(graph, dimension, games, rand)
    //shuffleByInnerBoxes(graph, dimension, games, rand)
}

private fun shuffleByFlipping(graph: SudokuGraph, dimension: Dimension, rand: Random) {
    val length = dimension.length
    val range = 0 until length

    if (rand.nextBoolean()) {
        verticalFlip(graph, length, range)
    }

    if (rand.nextBoolean()) {
        horizontalFlip(graph, length, range)
    }
}

private fun verticalFlip(graph: SudokuGraph, length: Int, range: IntRange) {
    for (rowIndex1 in range) {
        for (colIndex in range) {
            val rowIndex2 = length - rowIndex1 - 1

            val node1 = graph.get(rowIndex1, colIndex)
            val node2 = graph.get(rowIndex2, colIndex)

            swapNodeValue(node1, node2)
        }
    }
}

private fun horizontalFlip(graph: SudokuGraph, length: Int, range: IntRange) {
    for (colIndex1 in range) {
        for (rowIndex in range) {
            val colIndex2 = length - colIndex1 - 1

            val node1 = graph.get(rowIndex, colIndex1)
            val node2 = graph.get(rowIndex, colIndex2)

            swapNodeValue(node1, node2)
        }
    }
}

private fun shuffleByTransposing(graph: SudokuGraph, dimension: Dimension, rand: Random) {
    if (rand.nextBoolean()) {
        doTranspose(graph, dimension.length)
    }
}

private fun doTranspose(graph: SudokuGraph, length: Int) {
    val range = 0 until length

    for (rowIndex in range) {
        for (colIndex in range) {
            val node1 = graph.get(rowIndex, colIndex)
            val node2 = graph.get(colIndex, rowIndex)

            swapNodeValue(node1, node2)
        }
    }
}

private fun shuffleByRotating(graph: SudokuGraph, dimension: Dimension, rand: Random) {
    val amountOfRotations = rand.nextInt(4)
    val length = dimension.length

    repeat(amountOfRotations) { _ ->
        rotate90(graph, length)
    }
}

private fun rotate90(graph: SudokuGraph, length: Int) {
    horizontalFlip(graph, length, 0 until length)
    doTranspose(graph, length)
}

private fun shuffleBySwapping(graph: SudokuGraph, dimension: Dimension, rand: Random) {
    val shuffledMapping = createShuffledMapping(dimension.length, rand)
    val associations = groupNodesByValue(graph)

    for (value in 1 .. dimension.length) {
        val newValue = shuffledMapping.getValue(value)
        val nodes = associations.getValue(value)

        for (current in nodes) {
            current.value = newValue
        }
    }
}

private fun createShuffledMapping(length: Int, rand: Random): Map<Int, Int> {
    val values = (1 .. length).toMutableList()
    val map = HashMap<Int, Int>(length)

    for (value in 1 .. length) {
        val index = rand.nextInt(values.size)
        val randomValue = values.removeAt(index)

        map[value] = randomValue
    }

    return map
}

private fun groupNodesByValue(graph: SudokuGraph): Map<Int, List<SudokuNode>> {
    val length = graph.length
    val map = HashMap<Int, List<SudokuNode>>(length)

    for (value in 1 .. length) {
        val nodeList = ArrayList<SudokuNode>(length)

        for (node in graph) {
            if (value == node.value) {
                nodeList.add(node)
            }
        }

        map[value] = nodeList
    }

    return map
}

private fun shuffleByBoxes(graph: SudokuGraph, dimension: Dimension, games: Set<Game>, rand: Random) {
    if (Game.HYPER in games) {
        return
    }

    doRowBoxShuffle(graph, dimension, rand)
    doColBoxShuffle(graph, dimension, rand)
}

private fun doRowBoxShuffle(graph: SudokuGraph, dimension: Dimension, rand: Random) {
    val length = dimension.length
    val colSize = dimension.boxCols

    for (startColIndex1 in 0 until length step colSize) {
        val endColIndex1 = startColIndex1 + colSize
        val colRange1 = startColIndex1 until endColIndex1

        for (startColIndex2 in endColIndex1 until length step colSize) {
            if (rand.nextBoolean()) {
                continue
            }

            val colRange2 = startColIndex2 up colSize

            doRowBoxShuffleHelper(graph, colRange1, colRange2, length)
        }
    }
}

private fun doRowBoxShuffleHelper(graph: SudokuGraph, colRange1: IntRange, colRange2: IntRange, length: Int) {
    val col1 = colRange1.iterator()
    val col2 = colRange2.iterator()

    while (col1.hasNext() && col2.hasNext()) {
        val colIndex1 = col1.next()
        val colIndex2 = col2.next()

        for (rowIndex in 0 until length) {
            val node1 = graph.get(rowIndex, colIndex1)
            val node2 = graph.get(rowIndex, colIndex2)

            swapNodeValue(node1, node2)
        }
    }
}

private fun doColBoxShuffle(graph: SudokuGraph, dimension: Dimension, rand: Random) {
    val length = dimension.length
    val rowSize = dimension.boxRows

    for (startRowIndex1 in 0 until length step rowSize) {
        val endRowIndex1 = startRowIndex1 + rowSize
        val rowRange1 = startRowIndex1 until endRowIndex1

        for (startRowIndex2 in endRowIndex1 until length step rowSize) {
            if (rand.nextBoolean()) {
                continue
            }

            val rowRange2 = startRowIndex2 up rowSize

            doColBoxShuffleHelper(graph, rowRange1, rowRange2, length)
        }
    }
}

private fun doColBoxShuffleHelper(graph: SudokuGraph, rowRange1: IntRange, rowRange2: IntRange, length: Int) {
    val row1 = rowRange1.iterator()
    val row2 = rowRange2.iterator()

    while (row1.hasNext() && row2.hasNext()) {
        val rowIndex1 = row1.next()
        val rowIndex2 = row2.next()

        for (colIndex in 0 until length) {
            val node1 = graph.get(rowIndex1, colIndex)
            val node2 = graph.get(rowIndex2, colIndex)

            swapNodeValue(node1, node2)
        }
    }
}

private fun swapNodeValue(node1: SudokuNode, node2: SudokuNode) {
    val temp = node1.value
    node1.value = node2.value
    node2.value = temp
}
