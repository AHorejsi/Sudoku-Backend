package com.alexh.game

enum class SudokuDimension {
    NINE,
    TEN,
    TWELVE,
    FOURTEEN,
    FIFTEEN,
    SIXTEEN,
    EIGHTEEN,
    TWENTY,
    TWENTY_TWO,
    TWENTY_FOUR,
    TWENTY_FIVE
}

enum class SudokuDifficulty {
    BEGINNER,
    EASY,
    MEDIUM,
    HARD,
    MASTER
}

enum class SudokuKind {
    REGULAR,
    KILLER,
    HYPER,
    JIGSAW
}

class SudokuInfo(
    val dimension: SudokuDimension,
    val difficulty: SudokuDifficulty,
    val kind: SudokuKind
)
