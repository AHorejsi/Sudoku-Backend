package com.alexh.game

private class ExactCoverNode(column: ExactCoverNode? = null) {
    private var up: ExactCoverNode = this
    private var down: ExactCoverNode = this
    private var left: ExactCoverNode = this
    private var right: ExactCoverNode = this
    private var column: ExactCoverNode = column ?: this
}
