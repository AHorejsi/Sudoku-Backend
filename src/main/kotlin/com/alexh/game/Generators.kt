package com.alexh.game

fun regular(info: RegularInfo): RegularSudoku {
    val puzzle = RegularSudoku(info)

    initializeValues(puzzle)
    adjustForDifficulty(puzzle)
    shuffleBoard(puzzle)
    finalizePuzzle(puzzle)

    return puzzle
}

private fun finalizePuzzle(puzzle: RegularSudoku) {
    for (cell in puzzle.table) {
        if (null !== cell.value) {
            cell.editable = false
        }
    }
}

/*
fun killer(info: KillerInfo): KillerSudoku {

}

fun hyper(info: HyperInfo): HyperSudoku {

}

fun jigsaw(info: JigsawInfo): JigsawSudoku {

}
*/