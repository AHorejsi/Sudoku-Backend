package com.alexh.game

import com.alexh.utils.Position

internal fun makeCages(
    solved: List<List<Int>>,
    info: MakeSudokuCommand
): Set<Cage>? =
    if (Game.KILLER in info.games) {
        makeCagesHelper1(solved, info)
    }
    else {
        null
    }

private fun makeCagesHelper1(
    solved: List<List<Int>>,
    info: MakeSudokuCommand
): Set<Cage> {
    val cages = mutableSetOf<Cage>()
    val minCageCount = (info.dimension.length * info.difficulty.minCageSize).toInt()
    val maxCageCount = (info.dimension.length * info.difficulty.maxCageSize).toInt()
    val cageCountRange = minCageCount .. maxCageCount

    val initial = Position(0, 0)
    val current = mutableSetOf<Position>()
    val seen = mutableSetOf<Position>()

    makeCagesHelper2(cages, solved, initial, current, seen, info, cageCountRange)

    return cages
}

private fun makeCagesHelper2(
    cages: MutableSet<Cage>,
    solved: List<List<Int>>,
    current: Position,
    cagePos: MutableSet<Position>,
    seen: MutableSet<Position>,
    info: MakeSudokuCommand,
    cageCountRange: IntRange
) {
    if (current in seen || current.outOfBounds(info.dimension.length)) {
        return
    }

    seen.add(current)
    cagePos.add(current)

    var newCagePos = cagePos
    val mustStartNewCage = newCagePos.size >= cageCountRange.first || newCagePos.size <= cageCountRange.last && info.random.nextBoolean()

    if (mustStartNewCage) {
        val sum = cagePos.sumOf{ solved[it.rowIndex][it.colIndex] }

        val newCage = Cage(sum, cagePos)
        cages.add(newCage)

        newCagePos = mutableSetOf()
    }

    val positions = listOf(current.up, current.down, current.left, current.right).shuffled(info.random)

    for (pos in positions) {
        makeCagesHelper2(cages, solved, pos, newCagePos, seen, info, cageCountRange)
    }
}
