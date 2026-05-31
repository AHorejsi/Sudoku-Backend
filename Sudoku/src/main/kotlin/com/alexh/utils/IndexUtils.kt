package com.alexh.utils

import kotlinx.serialization.Serializable

@Serializable
data class Position(val rowIndex: Int, val colIndex: Int) : Comparable<Position> {
    val up: Position
        get() = Position(this.rowIndex - 1, this.colIndex)

    val down: Position
        get() = Position(this.rowIndex + 1, this.colIndex)

    val left: Position
        get() = Position(this.rowIndex, this.colIndex - 1)

    val right: Position
        get() = Position(this.rowIndex, this.colIndex + 1)

    override operator fun compareTo(other: Position): Int {
        val rowComp = this.rowIndex - other.rowIndex

        return if (0 != rowComp)
            rowComp
        else
            this.colIndex - other.colIndex
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Position) {
            return false
        }

        return this.rowIndex == other.rowIndex && this.colIndex == other.colIndex
    }

    override fun hashCode(): Int =
        listOf(this.rowIndex, this.colIndex).hashCode()
}

fun outOfBounds(index: Int, length: Int): Boolean =
    index < 0 || index >= length

fun outOfBounds(pos: Position, length: Int): Boolean =
    outOfBounds(pos.rowIndex, length) || outOfBounds(pos.colIndex, length)

infix fun Int.up(amount: Int): IntRange =
    if (amount < 0)
        throw IllegalArgumentException("Cannot count up by a negative amount. Amount: $amount")
    else
        this until (this + amount)

fun IntProgression.thru(other: IntProgression): Iterable<Pair<Int, Int>> =
    object : Iterable<Pair<Int, Int>> {
        override fun iterator(): Iterator<Pair<Int, Int>> =
            ZippedIterator(this@thru, other)
    }

private class ZippedIterator(
    left: IntProgression,
    right: IntProgression
) : Iterator<Pair<Int, Int>> {
    private val leftIter = left.iterator()
    private val rightIter = right.iterator()

    override fun hasNext(): Boolean =
        this.leftIter.hasNext() && this.rightIter.hasNext()

    override fun next(): Pair<Int, Int> {
        if (!this.hasNext()) {
            throw NoSuchElementException("No more indices")
        }

        val leftElem = this.leftIter.next()
        val rightElem = this.rightIter.next()

        return Pair(leftElem, rightElem)
    }
}

fun IntProgression.pair(other: IntProgression): Iterable<Position> =
    object : Iterable<Position> {
        override fun iterator(): Iterator<Position> =
            NestedIntProgressionIterator(this@pair, other)
    }

private class NestedIntProgressionIterator(
    left: IntProgression,
    private val right: IntProgression
) : Iterator<Position> {

    private val actualLeft: IntIterator
    private var currentLeftItem: Int
    private var actualRight: IntIterator

    init {
        if (left.isEmpty() || this.right.isEmpty()) {
            this.actualLeft = intArrayOf().iterator()
            this.currentLeftItem = -1
            this.actualRight = intArrayOf().iterator()
        }
        else {
            this.actualLeft = left.iterator()
            this.currentLeftItem = this.actualLeft.nextInt()
            this.actualRight = this.right.iterator()
        }
    }

    override fun hasNext(): Boolean =
        this.actualLeft.hasNext() || this.actualRight.hasNext()

    override fun next(): Position {
        if (!this.hasNext()) {
            throw NoSuchElementException("No more indices")
        }

        if (!this.actualRight.hasNext()) {
            this.currentLeftItem = this.actualLeft.nextInt()
            this.actualRight = this.right.iterator()
        }

        val rightItem = this.actualRight.nextInt()

        return Position(this.currentLeftItem, rightItem)
    }
}
