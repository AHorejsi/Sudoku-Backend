package com.alexh

import com.alexh.utils.Position
import com.alexh.utils.get2d
import kotlin.test.Test
import kotlin.test.assertEquals

class ListTest {
    @Test
    fun testGet2d() {
        val lengths = arrayOf(1, 2, 5, 8, 10, 15, 20)

        for (rows in lengths) {
            for (cols in lengths) {
                val list = List(rows * cols) { as2d(it, cols) }

                this.runGet2d(list, rows, cols)
            }
        }
    }

    private fun as2d(index: Int, cols: Int): Position =
        Position(index / cols, index % cols)

    private fun runGet2d(list: List<Position>, rows: Int, cols: Int) {
        for (rowIndex in 0 until rows) {
            for (colIndex in 0 until cols) {
                val item = list.get2d(rowIndex, colIndex, cols)
                val pos = Position(rowIndex, colIndex)

                assertEquals(pos, item)
            }
        }
    }
}
