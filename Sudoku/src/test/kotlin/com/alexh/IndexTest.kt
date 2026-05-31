package com.alexh

import com.alexh.asserts.assertLess
import com.alexh.asserts.assertGreater
import com.alexh.asserts.assertGreaterOrEqual
import com.alexh.asserts.assertLessOrEqual
import com.alexh.utils.*
import kotlin.math.min
import kotlin.test.*

class IndexTest {
    @Test
    fun testUp() {
        val testSet = arrayOf(0, 5, 10, 50, 100, 500, 1000)

        for (start in testSet) {
            for (jump in testSet) {
                val range = start up jump
                val end = start + jump - 1

                assertEquals(jump, range.count())
                assertEquals(start, range.first)
                assertEquals(end, range.last)
            }
        }

        assertFailsWith<IllegalArgumentException>{ 0 up -1 }
    }

    @Test
    fun testOutOfBounds() {
        val maxLength = 25
        val indexRange = 0 until maxLength
        val paired = indexRange.pair(indexRange)

        for (pos in paired) {
            val rowIndex = pos.rowIndex
            val colIndex = pos.colIndex

            val rowShouldBeOutOfBounds = rowIndex < 0 || rowIndex >= maxLength
            val colShouldBeOutOfBounds = colIndex < 0 || colIndex >= maxLength

            val rowResult = outOfBounds(rowIndex, maxLength)
            val rowMessage = "RowIndex: $rowIndex"
            this.checkIndexingResult(rowResult, rowMessage, rowShouldBeOutOfBounds)

            val colResult = outOfBounds(colIndex, maxLength)
            val colMessage = "ColIndex: $colIndex"
            this.checkIndexingResult(colResult, colMessage, colShouldBeOutOfBounds)

            val posResult = outOfBounds(pos, maxLength)
            val posMessage = "Position: ($rowIndex, $colIndex)"
            this.checkIndexingResult(posResult, posMessage, rowShouldBeOutOfBounds || colShouldBeOutOfBounds)
        }
    }

    private fun checkIndexingResult(result: Boolean, message: String, shouldHaveSucceeded: Boolean) {
        if (shouldHaveSucceeded) {
            assertTrue(result, message)
        } else {
            assertFalse(result, message)
        }
    }

    @Test
    fun testRelativePositions() {
        val maxLength = 50
        val indexRange = 0 until maxLength

        for (rowIndex in indexRange) {
            for (colIndex in indexRange) {
                val pos = Position(rowIndex, colIndex)

                this.testRelativePositionsOfCurrentPosition(pos)
            }
        }
    }

    private fun testRelativePositionsOfCurrentPosition(pos: Position) {
        val up = pos.up
        assertEquals(pos.rowIndex - 1, up.rowIndex)
        assertEquals(pos.colIndex, up.colIndex)

        val down = pos.down
        assertEquals(pos.rowIndex + 1, down.rowIndex)
        assertEquals(pos.colIndex, down.colIndex)

        val left = pos.left
        assertEquals(pos.rowIndex, left.rowIndex)
        assertEquals(pos.colIndex - 1, left.colIndex)

        val right = pos.right
        assertEquals(pos.rowIndex, right.rowIndex)
        assertEquals(pos.colIndex + 1, right.colIndex)
    }

    @Test
    fun testThru() {
        val min = 1
        val max = 125

        for (index in 0 until max - min + 1) {
            val ranges = arrayOf(
                min .. index,
                index downTo min,
                index .. max,
                max downTo index,
                min .. max,
                max downTo min
            )

            this.testThruOrder(ranges)
        }
    }

    private fun testThruOrder(ranges: Array<IntProgression>) {
        for (range1 in ranges) {
            for (range2 in ranges) {
                val thruSet = range1.thru(range2)

                this.testSizeOfThru(range1, range2, thruSet)
                this.testElementsOfThru(range1, range2, thruSet)
            }
        }
    }

    private fun testSizeOfThru(range1: IntProgression, range2: IntProgression, thruSet: Iterable<Pair<Int, Int>>) {
        val range1Size = range1.count()
        val range2Size = range2.count()
        val thruSize = thruSet.count()

        val minSize = min(range1Size, range2Size)

        assertEquals(minSize, thruSize)
    }

    private fun testElementsOfThru(range1: IntProgression, range2: IntProgression, thruSet: Iterable<Pair<Int, Int>>) {
        val range1Iter = range1.iterator()
        val range2Iter = range2.iterator()
        val thruIter = thruSet.iterator()

        while (range1Iter.hasNext() && range2Iter.hasNext()) {
            val elem1 = range1Iter.next()
            val elem2 = range2Iter.next()
            val (thruLeft, thruRight) = thruIter.next()

            assertEquals(elem1, thruLeft)
            assertEquals(elem2, thruRight)
        }

        val moreInThruSet = thruIter.hasNext()

        assertFalse(moreInThruSet)
    }

    @Test
    fun testPair() {
        val min = 1
        val max = 100

        for (index in 0 until max - min + 1) {
            val ranges = arrayOf(
                min .. index,
                index downTo min,
                index .. max,
                max downTo index,
                min .. max,
                max downTo min
            )

            this.testPairOrder(ranges)
        }
    }

    private fun testPairOrder(progressions: Array<IntProgression>) {
        for (range1 in progressions) {
            for (range2 in progressions) {
                val paired = range1.pair(range2)

                this.testSizeOfPair(range1, range2, paired)
                this.testElementsOfPair(range1, range2, paired)
            }
        }
    }

    private fun testSizeOfPair(range1: IntProgression, range2: IntProgression, paired: Iterable<Position>) {
        if (range1.isEmpty() || range2.isEmpty()) {
            val empty = !paired.any()

            assertTrue(empty)
        }
        else {
            val range1Size = range1.count()
            val range2Size = range2.count()
            val pairedSize = paired.count()

            val collectiveRangeSize = range1Size * range2Size
            val message = "Range1: $range1Size, Range2: $range2Size, Paired: $pairedSize"

            assertEquals(collectiveRangeSize, pairedSize, message)
        }
    }

    private fun testElementsOfPair(range1: IntProgression, range2: IntProgression, paired: Iterable<Position>) {
        val pairedIter = paired.iterator()

        for (index1 in range1) {
            for (index2 in range2) {
                val pos = pairedIter.next()

                assertEquals(index1, pos.rowIndex)
                assertEquals(index2, pos.colIndex)
            }
        }
    }

    @Test
    fun testEqualityFunctions() {
        val maxLength = 75
        val endIndex = maxLength - 1
        val indexRange = 0 until maxLength
        val paired = indexRange.pair(indexRange).iterator()

        for (rowIndex in indexRange) {
            for (colIndex in indexRange) {
                val pos1 = Position(rowIndex, colIndex)
                val pos2 = paired.next()

                this.testEquivalentPositionsForEquality(pos1, pos2)
                this.testAdjacentPositionsForEquality(pos1, pos2, endIndex)
            }
        }
    }

    private fun testEquivalentPositionsForEquality(pos1: Position, pos2: Position) {
        assertEquals(pos1, pos2)

        val hash1 = pos1.hashCode()
        val hash2 = pos2.hashCode()

        assertEquals(hash1, hash2)
    }

    @Suppress("SameParameterValue")
    private fun testAdjacentPositionsForEquality(pos1: Position, pos2: Position, endIndex: Int) {
        if (0 != pos1.rowIndex || 0 != pos2.rowIndex) {
            this.testEquivalentPositionsForEquality(pos1.up, pos2.up)
        }

        if (endIndex == pos1.rowIndex || endIndex == pos2.rowIndex) {
            this.testEquivalentPositionsForEquality(pos1.down, pos2.down)
        }

        if (0 != pos1.colIndex || 0 != pos2.colIndex) {
            this.testEquivalentPositionsForEquality(pos1.left, pos2.left)
        }

        if (endIndex == pos1.colIndex || endIndex == pos2.colIndex) {
            this.testEquivalentPositionsForEquality(pos1.right, pos2.right)
        }

        val adjacent1 = listOf(pos1.up, pos1.down, pos1.left, pos1.right)
        val adjacent2 = listOf(pos2.up, pos2.down, pos2.left, pos2.right)

        for ((index1, nextPos1) in adjacent1.withIndex()) {
            for ((index2, nextPos2) in adjacent2.withIndex()) {
                this.testEqualityProperties(nextPos1, index1, nextPos2, index2)
            }
        }
    }

    private fun testEqualityProperties(
        pos1: Position,
        index1: Int,
        pos2: Position,
        index2: Int
    ) {
        if (index1 != index2) {
            assertNotEquals(pos1, pos2)
        } else {
            val hash1 = pos1.hashCode()
            val hash2 = pos2.hashCode()

            assertEquals(pos1, pos1)
            assertEquals(hash1, hash2)
        }
    }

    @Test
    fun testCompareTo() {
        val maxLength = 150
        val indexRange = 0 until maxLength

        for (rowIndex in indexRange) {
            for (colIndex in indexRange) {
                val pos = Position(rowIndex, colIndex)

                this.compareAdjacentPositions(pos)
                this.comparePositionsByRowAndColumn(pos, maxLength)

                assertLessOrEqual(pos, pos)
                assertGreaterOrEqual(pos, pos)
                assertEquals(0, pos.compareTo(pos))
            }
        }
    }

    private fun compareAdjacentPositions(pos: Position) {
        assertLess(pos.up, pos)
        assertLess(pos.left, pos)
        assertLess(pos, pos.down)
        assertLess(pos, pos.right)
    }

    @Suppress("SameParameterValue")
    private fun comparePositionsByRowAndColumn(pos: Position, maxLength: Int) {
        for (rowIndex in 0 until pos.rowIndex) {
            for (colIndex in 0 until pos.colIndex) {
                val before = Position(rowIndex, colIndex)

                assertLess(before, pos)
                assertGreater(pos, before)
            }
        }

        for (rowIndex in pos.rowIndex + 1 until maxLength) {
            for (colIndex in pos.colIndex + 1 until maxLength) {
                val after = Position(rowIndex, colIndex)

                assertLess(pos, after)
                assertGreater(after, pos)
            }
        }
    }
}
