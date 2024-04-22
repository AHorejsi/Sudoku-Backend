package com.alexh.game

internal fun hasUniqueSolution(
    neighborhoods: List<SudokuNode>,
    length: Int
): Boolean {
    val unassigned = neighborhoods.asSequence().filter{ null === it.value }.toMutableList()
    val range = 1 .. length
    val valid = findValidValues(unassigned, range)

    return 1 == countSolutions(unassigned, valid)
}

private fun findValidValues(
    unassigned: MutableList<SudokuNode>,
    range: IntRange
): MutableMap<SudokuNode, MutableSet<Int>> {
    val valid = mutableMapOf<SudokuNode, MutableSet<Int>>()

    for (node in unassigned) {
        val values = range.toHashSet()

        for (neighbor in node.neighbors) {
            neighbor.value?.let {
                values.remove(it)
            }
        }

        valid[node] = values
    }

    return valid
}

private fun countSolutions(
    unassigned: MutableList<SudokuNode>,
    valid: MutableMap<SudokuNode, MutableSet<Int>>
): Int {
    if (unassigned.isEmpty()) {
        return 1
    }

    var found = 0
    val node = unassigned.removeLast()

    for (value in valid.getValue(node)) {
        node.value = value
        found += countSolutions(unassigned, valid)
        node.value = null

        if (found > 1) {
            break
        }
    }

    unassigned.add(node)

    return found
}
