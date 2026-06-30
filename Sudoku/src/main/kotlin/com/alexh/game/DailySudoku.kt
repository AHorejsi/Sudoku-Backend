package com.alexh.game

import com.alexh.utils.now
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar

private val MUTEX = Mutex()

private var lastDailyCall = now
private val dailyPuzzles= runBlocking {
    val map = mutableMapOf<MakeSudokuCommand, SudokuJson>()

    generateDailies(this, map)

    return@runBlocking map
}

suspend fun retrieveDailySudoku(info: MakeSudokuCommand, scope: CoroutineScope): SudokuJson {
    MUTEX.withLock {
        val newDailyCall = now

        if (newDailyCall.get(Calendar.DAY_OF_MONTH) > lastDailyCall.get(Calendar.DAY_OF_MONTH)) {
            generateDailies(scope, dailyPuzzles)
        }

        lastDailyCall = newDailyCall
    }

    return dailyPuzzles[info]!!
}

private fun generateDailies(scope: CoroutineScope, dailies: MutableMap<MakeSudokuCommand, SudokuJson>) {
    val dimensionArray = Dimension.states
    val difficultyArray = Difficulty.states
    val gameSubsets = Game.subsets

    for (dimension in dimensionArray) {
        for (difficulty in difficultyArray) {
            for (games in gameSubsets) {
                val info = MakeSudokuCommand(dimension, difficulty, games)

                scope.launch { dailies[info] = makeSudoku(info) }
            }
        }
    }
}
