package com.alexh.game

internal fun hasUniqueSolution(
    neighborhoods: List<SudokuNode>,
    length: Int
): Boolean {
    val unvisited = neighborhoods.asSequence().filter{ null === it.value }.toMutableList()
    val range = 1 .. length

    return countSolutions(unvisited, range) >= 1
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

    for (value in range) {
        var safe = true

        for (neighbor in node.neighbors) {
            if (value == neighbor.value) {
                safe = false

                break
            }
        }

        if (safe) {
            node.value = value

            found += countSolutions(unvisited, range)

            if (found > 1) {
                break
            }

            node.value = null
        }
    }

    unvisited.add(node)

    return found
}
