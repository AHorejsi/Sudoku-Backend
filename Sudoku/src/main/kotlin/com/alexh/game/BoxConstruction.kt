package com.alexh.game

import com.alexh.utils.Position

internal fun constructBoxSet(graph: SudokuGraph, games: Set<Game>): Set<Box> {
    val boxSet = HashSet<Box>(graph.length)

    makeBoxes(graph, boxSet)

    if (Game.HYPER in games) {
        makeHyperBoxes(graph, boxSet)
    }

    return boxSet
}

private fun makeBoxes(graph: SudokuGraph, boxSet: MutableSet<Box>) {
    val seen = HashSet<Position>(graph.size)

    for (node in graph) {
        if (node.place in seen) {
            continue
        }

        val positionsInBox = HashSet<Position>(graph.length)
        includeInBox(node, positionsInBox, seen)

        for (neighborNode in node.box) {
            includeInBox(neighborNode, positionsInBox, seen)
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
        includeInBox(node, positionsInBox, seen)

        for (neighborNode in node.hyper) {
            includeInBox(neighborNode, positionsInBox, seen)
        }

        val newBox = Box(true, positionsInBox)

        boxSet.add(newBox)
    }
}

private fun includeInBox(node: SudokuNode, boxPos: MutableSet<Position>, seen: MutableSet<Position>) {
    boxPos.add(node.place)
    seen.add(node.place)
}
