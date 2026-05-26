package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.outOfBounds
import kotlin.collections.HashSet
import kotlin.random.Random

internal fun formCages(solved: List<List<Int>>, info: MakeSudokuCommand): Set<Cage>? {
    if (Game.KILLER !in info.games) {
        return null
    }

    val length = info.dimension.length
    val cellCount = length * length

    val cageSizeRange = retrieveCageSizeRange(info, length)
    val cages = HashSet<Cage>(cellCount)

    makeCagesHelper(cages, solved, length, info.random, cageSizeRange)

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
    solved: List<List<Int>>,
    length: Int,
    rand: Random,
    cageSizeRange: IntRange
) {
    val available = retrievePositions(length)

    while (available.isNotEmpty()) {
        val cageSize = rand.nextInt(cageSizeRange.first, cageSizeRange.last)

        val cagePos = HashSet<Position>(cageSize)
        var pos = selectPositionRandomly(available, rand)

        while (cagePos.size < cageSize) {
            cagePos.add(pos)
            available.remove(pos)

            val adjacent = findAdjacentPositions(pos, length, available)

            if (adjacent.isEmpty()) {
                break
            }

            pos = selectPositionRandomly(adjacent, rand)
        }

        val sum = getSumForCage(cagePos, solved)
        val newCage = Cage(sum, cagePos)

        cages.add(newCage)
    }
}

private fun retrievePositions(length: Int): MutableList<Position> {
    val positions = mutableListOf<Position>()
    val range = 0 until length

    for (rowIndex in range) {
        for (colIndex in range) {
            val newPos = Position(rowIndex, colIndex)

            positions.add(newPos)
        }
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

private fun getSumForCage(cagePos: Set<Position>, solved: List<List<Int>>): Int {
    var sum = 0

    for (pos in cagePos) {
        val value = solved[pos.rowIndex][pos.colIndex]

        sum += value
    }

    return sum
}
