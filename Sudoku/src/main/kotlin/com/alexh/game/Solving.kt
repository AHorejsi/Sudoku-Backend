package com.alexh.game

internal fun hasUniqueSolution(graph: SudokuGraph, length: Int): Boolean {
    val unassigned = findNodesWithNullValues(graph, length)
    val values = 1 .. length

    return 1 == countSolutions(unassigned, values)
}

private fun findNodesWithNullValues(graph: SudokuGraph, length: Int): MutableList<SudokuNode> {
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

        if (found > 1) {
            break
        }
    }

    unassigned.add(node)

    return found
}

private fun findValidValues(node: SudokuNode, valueRange: IntRange): Set<Int> {
    val valid = valueRange.toHashSet()

    for (neighbor in node.all) {
        neighbor.value?.let {
            valid.remove(it)
        }
    }

    return valid
}
