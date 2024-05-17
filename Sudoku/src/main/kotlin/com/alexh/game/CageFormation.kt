package com.alexh.game

import com.alexh.utils.Position
import com.alexh.utils.get2d
import kotlin.collections.HashSet
import kotlin.random.Random

internal fun makeCages(
    neighborhoods: List<SudokuNode>,
    info: MakeSudokuCommand
): Set<Cage>? {
    if (Game.KILLER !in info.games) {
        return null
    }

    val length = info.dimension.length

    val cageRange = decideCageRange(info, length)
    val cages = HashSet<Cage>(length * length)

    makeCagesHelper(cages, neighborhoods, info.random, length, cageRange)

    return cages
}

private fun decideCageRange(
    info: MakeSudokuCommand,
    length: Int
): IntRange {
    val minCageCount = (length * info.difficulty.minCageSize).toInt()
    val maxCageCount = (length * info.difficulty.maxCageSize).toInt()

    return minCageCount .. maxCageCount
}

private fun makeCagesHelper(
    cages: MutableSet<Cage>,
    neighborhoods: List<SudokuNode>,
    rand: Random,
    length: Int,
    cageRange: IntRange
) {
    val available = makeAllPositions(length)

    while (available.any()) {
        val cageSize = cageRange.random(rand)

        val cagePos = HashSet<Position>(cageSize)
        var pos = available.random(rand)

        while (cagePos.size < cageSize) {
            cagePos.add(pos)
            available.remove(pos)

            val adjacent = listOf(pos.up, pos.down, pos.left, pos.right)
                .asSequence()
                .filterNot{ it.outOfBounds(length) }
                .filter{ it in available }
                .toList()

            if (adjacent.isEmpty()) {
                break
            }

            pos = adjacent.random(rand)
        }

        val sum = cagePos.sumOf{ neighborhoods.get2d(it.rowIndex, it.colIndex, length).value!! }
        val newCage = Cage(sum, cagePos)

        cages.add(newCage)
    }
}

private fun makeAllPositions(
    length: Int
): MutableList<Position> {
    val range = (0 until length).asSequence()

    return range.map{ rowIndex ->
        range.map{ colIndex ->
            Position(rowIndex, colIndex)
        }
    }.flatten().toMutableList()
}
