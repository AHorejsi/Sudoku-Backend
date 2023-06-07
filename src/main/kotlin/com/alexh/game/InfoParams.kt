package com.alexh.game

import kotlinx.serialization.Serializable

@Serializable
enum class SudokuGame {
    REGULAR,
    KILLER,
    HYPER,
    JIGSAW
}
