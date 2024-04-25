package com.alexh.game

internal fun hasUniqueSolution(
    neighborhoods: List<SudokuNode>,
    length: Int
): Boolean {
    val unassigned = neighborhoods.asSequence().filter{ null === it.value }.toMutableList()
    val values = 1 .. length

    val solutionCount = countSolutions(unassigned, values)

    return 1 == solutionCount
}

private fun countSolutions(
    unassigned: MutableList<SudokuNode>,
    valueSet: IntRange
): Int {
    if (unassigned.isEmpty()) {
        return 1
    }

    var found = 0
    val node = unassigned.removeLast()

    outer@
    for (value in valueSet) {
        for (neighbor in node.neighbors) {
            if (value == neighbor.value) {
                continue@outer
            }
        }

        node.value = value
        found += countSolutions(unassigned, valueSet)
        node.value = null

        if (found > 1) {
            break
        }
    }

    unassigned.add(node)

    return found
}
