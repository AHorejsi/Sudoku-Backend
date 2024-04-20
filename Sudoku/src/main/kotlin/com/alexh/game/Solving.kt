package com.alexh.game

internal fun hasUniqueSolution(
    neighborhoods: List<SudokuNode>,
    length: Int
): Boolean {
    val unvisited = neighborhoods.asSequence().filter{ null === it.value }.toMutableList()
    val range = 1 .. length

    return 1 == countSolutions(unvisited, range)
}

private fun countSolutions(
    unvisited: MutableList<SudokuNode>,
    range: IntRange
): Int {
    if (unvisited.isEmpty()) {
        return 1
    }

    var found = 0
    val node = unvisited.removeLast()

    outer@
    for (value in range) {
        for (neighbor in node.neighbors) {
            if (value == neighbor.value) {
                continue@outer
            }
        }

        node.value = value
        found += countSolutions(unvisited, range)
        node.value = null

        if (found > 1) {
            break
        }
    }

    unvisited.add(node)

    return found
}
