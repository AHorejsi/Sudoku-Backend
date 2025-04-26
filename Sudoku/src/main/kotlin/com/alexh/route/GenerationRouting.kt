package com.alexh.route

import com.alexh.game.*
import com.alexh.models.GenerateRequest
import com.alexh.models.GenerateResponse
import com.alexh.utils.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(Loggers.GENERATION_ROUTING)

fun configureEndpointsForGeneratingPuzzles(app: Application) {
    app.routing {
        this.post(Endpoints.GENERATE) {
            val result = generateSudoku(this.call)

            handleResult(result, this.call, logger, "Successful call to ${Endpoints.GENERATE}")
        }
    }
}

private suspend fun generateSudoku(call: ApplicationCall): Result<GenerateResponse> = runCatching {
    val request = call.receive(GenerateRequest::class)

    if (request.dimension.isEmpty() || request.difficulty.isEmpty()) {
        return@runCatching GenerateResponse.UnfilledFields
    }

    val dimension = Dimension.valueOf(request.dimension)
    val difficulty = Difficulty.valueOf(request.difficulty)
    val games = request.games.map{ Game.valueOf(it) }.toSortedSet()
    val info = MakeSudokuCommand(dimension, difficulty, games)

    val sudoku = makeSudoku(info)

    return@runCatching GenerateResponse.Success(sudoku)
}
