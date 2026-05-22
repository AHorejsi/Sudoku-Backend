package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.outOfBounds
import kotlin.collections.HashSet
import kotlin.random.Random

internal fun makeCages(graph: SudokuGraph, info: MakeSudokuCommand): Set<Cage>? {
    if (Game.KILLER !in info.games) {
        return null
    }

    val cageSizeRange = retrieveCageSizeRange(info, graph.length)
    val cages = HashSet<Cage>(graph.size)

    makeCagesHelper(cages, graph, info.random, cageSizeRange)

    return cages
}

private fun retrieveCageSizeRange(info: MakeSudokuCommand, length: Int): IntRange {
    val difficulty = info.difficulty

    val minCageCount = (length * difficulty.minCageSize).toInt()
    val maxCageCount = (length * difficulty.maxCageSize).toInt()

    return minCageCount .. maxCageCount
}

private fun makeCagesHelper(
    cages: MutableSet<Cage>,
    graph: SudokuGraph,
    rand: Random,
    cageSizeRange: IntRange
) {
    val available = retrievePositions(graph)

    while (available.isNotEmpty()) {
        val cageSize = rand.nextInt(cageSizeRange.first, cageSizeRange.last)

        val cagePos = HashSet<Position>(cageSize)
        var pos = selectPositionRandomly(available, rand)

        while (cagePos.size < cageSize) {
            cagePos.add(pos)
            available.remove(pos)

            val adjacent = findAdjacentPositions(pos, graph.length, available)

            if (adjacent.isEmpty()) {
                break
            }

            pos = selectPositionRandomly(adjacent, rand)
        }

        val sum = getSumForCage(cagePos, graph)
        val newCage = Cage(sum, cagePos)

        cages.add(newCage)
    }
}

private fun retrievePositions(graph: SudokuGraph): MutableList<Position> {
    val positions = ArrayList<Position>(graph.size)

    for (node in graph) {
        positions.add(node.place)
    }

    return positions
}

private fun selectPositionRandomly(list: List<Position>, rand: Random): Position {
    val randomIndex = rand.nextInt(list.size)

    return list[randomIndex]
}

private fun findAdjacentPositions(pos: Position, length: Int, available: List<Position>): List<Position> {
    val adjacent = mutableListOf<Position>()

    checkPosition(pos.up, length, available, adjacent)
    checkPosition(pos.down, length, available, adjacent)
    checkPosition(pos.left, length, available, adjacent)
    checkPosition(pos.right, length, available, adjacent)

    return adjacent
}

private fun checkPosition(pos: Position, length: Int, available: List<Position>, adjacent: MutableList<Position>) {
    if (!outOfBounds(pos, length) && pos in available) {
        adjacent.add(pos)
    }
}

private fun getSumForCage(cagePos: Set<Position>, graph: SudokuGraph): Int {
    var sum = 0

    for (pos in cagePos) {
        val node = graph.get(pos)

        sum += node.value!!
    }

    return sum
}
