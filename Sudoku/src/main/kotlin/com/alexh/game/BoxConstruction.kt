package com.alexh.game

import com.alexh.utils.Position

internal fun constructBoxSet(graph: SudokuGraph, games: Set<Game>): Set<Box> {
    val boxSet = HashSet<Box>(graph.length)

    makeRegularBoxes(graph, boxSet)

    if (Game.HYPER in games) {
        makeHyperBoxes(graph, boxSet)
    }

    return boxSet
}

private fun makeRegularBoxes(graph: SudokuGraph, boxSet: MutableSet<Box>) {
    val length = graph.length
    val seen = HashSet<Position>(graph.size)

    for (node in graph) {
        if (node.place in seen) {
            continue
        }

        val positionsInBox = HashSet<Position>(length)

        for (neighborNode in node.box + node) {
            positionsInBox.add(neighborNode.place)

            seen.add(neighborNode.place)
        }

        val newBox = Box(false, positionsInBox)

        boxSet.add(newBox)
    }
}

private fun makeHyperBoxes(graph: SudokuGraph, boxSet: MutableSet<Box>) {
    val length = graph.length
    val seen = HashSet<Position>(length * length)

    for (node in graph) {
        if (node.place in seen || node.hyper.isEmpty()) {
            continue
        }

        val positionsInBox = HashSet<Position>(length)

        for (neighborNode in node.hyper + node) {
            positionsInBox.add(neighborNode.place)

            seen.add(neighborNode.place)
        }

        val newBox = Box(true, positionsInBox)

        boxSet.add(newBox)
    }
}
