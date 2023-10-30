package com.alexh.game

import kotlinx.serialization.Serializable

@Serializable
class SudokuJson(
    val table: Array<Int?>,
    val length: Int,
    val boxRows: Int,
    val boxCols: Int,
    val difficulty: String,
    val type: String
)