package com.alexh.game

internal fun hasUniqueSolution(
    neighborhoods: List<SudokuNode>,
    length: Int
): Boolean {
    val unassigned = neighborhoods.asSequence().filter{ null === it.value }.toMutableList()
    val values = 1 .. length

    return 1 == countSolutions(unassigned, values)
}

private fun countSolutions(
    unassigned: MutableList<SudokuNode>,
    valueRange: IntRange
): Int {
    if (unassigned.isEmpty()) {
        return 1
    }

    var found = 0
    val node = unassigned.removeLast()

    val valid = findValidValues(node, valueRange)

    for (value in valid) {
        node.value = value
        found += countSolutions(unassigned, valueRange)
        node.value = null

        if (found > 1) {
            break
        }
    }

    unassigned.add(node)

    return found
}

private fun findValidValues(
    node: SudokuNode,
    valueRange: IntRange
): MutableSet<Int> {
    val valid = valueRange.toMutableSet()

    for (neighbor in node.neighbors) {
        neighbor.value?.let {
            valid.remove(it)
        }
    }

    return valid
}
