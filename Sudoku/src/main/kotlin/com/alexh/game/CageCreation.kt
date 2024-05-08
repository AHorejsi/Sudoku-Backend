package com.alexh.game

import com.alexh.utils.Position

fun makeCages(
    solved: List<List<Int>>,
    info: MakeSudokuCommand
): Set<Cage>? {
    if (Game.KILLER !in info.games) {
        return null
    }

    val length = info.dimension.length
    val count = length * length

    val cageCountRange = decideCageCountRange(info, length)
    val initial = Position(0, 0)
    val cagePos = LinkedHashSet<Position>(cageCountRange.last)
    val seen = HashSet<Position>(count)
    val cages = HashSet<Cage>(count)

    makeCagesHelper(cages, solved, initial, cagePos, seen, info, cageCountRange)

    saveCage(solved, cagePos, cages)

    println("${cages.sumOf{ it.sum }} ${cages.sumOf{ it.positions.size }}")

    return cages
}

private fun decideCageCountRange(
    info: MakeSudokuCommand,
    length: Int
): IntRange {
    val minCageCount = (length * info.difficulty.minCageSize).toInt()
    val maxCageCount = (length * info.difficulty.maxCageSize).toInt()

    return minCageCount .. maxCageCount
}

private fun makeCagesHelper(
    cages: MutableSet<Cage>,
    solved: List<List<Int>>,
    current: Position,
    cagePos: MutableSet<Position>,
    seen: MutableSet<Position>,
    info: MakeSudokuCommand,
    cageCountRange: IntRange
) {
    if (current.outOfBounds(info.dimension.length) || current in seen) {
        return
    }

    seen.add(current)
    cagePos.add(current)

    var newCagePos = cagePos
    val mustStartNewCage = cagePos.size >= cageCountRange.first && info.random.nextBoolean() || cagePos.size >= cageCountRange.last

    if (mustStartNewCage) {
        saveCage(solved, cagePos, cages)

        newCagePos = LinkedHashSet(cageCountRange.last)
    }

    val positions = listOf(current.up, current.down, current.left, current.right).shuffled(info.random)

    for (pos in positions) {
        makeCagesHelper(cages, solved, pos, newCagePos, seen, info, cageCountRange)
    }
}

private fun saveCage(
    solved: List<List<Int>>,
    cagePos: MutableSet<Position>,
    cages: MutableSet<Cage>
) {
    val sum = cagePos.sumOf{ solved[it.rowIndex][it.colIndex] }
    val newCage = Cage(sum, cagePos)

    cages.add(newCage)
}
