package com.alexh.game

internal fun hasOneSolution(graph: SudokuGraph, length: Int): Boolean {
    val unassigned = findNodesWithNullValues(graph)
    val values = 1 .. length

    val solutionCount = countSolutions(unassigned, values)

    if (0 == solutionCount) {
        throw IllegalStateException("Failed to find any solutions")
    } else {
        return 1 == solutionCount
    }
}

private fun findNodesWithNullValues(graph: SudokuGraph): MutableList<SudokuNode> {
    val unassigned = ArrayList<SudokuNode>(graph.size)

    for (node in graph) {
        if (null === node.value) {
            unassigned.add(node)
        }
    }

    return unassigned
}

private fun countSolutions(unassigned: MutableList<SudokuNode>, valueRange: IntRange): Int {
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

        if (found > 1) { // Means there is more than one solution. Therefore, searching for more is not needed
            break
        }
    }

    unassigned.add(node)

    return found
}

private fun findValidValues(node: SudokuNode, valueRange: IntRange): Set<Int> {
    /*val valid = valueRange.toHashSet()

    for (neighbor in node.all) {
        neighbor.value?.let {
            valid.remove(it)
        }
    }

    return valid*/

    val values = node.all.asSequence().mapNotNull{ it.value }.toHashSet()
    val valid = valueRange.asSequence().filter{ it !in values }.toHashSet()

    return valid
}
