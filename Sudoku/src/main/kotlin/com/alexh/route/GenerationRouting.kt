package com.alexh.route

import com.alexh.game.*
import com.alexh.models.GenerateRequest
import com.alexh.models.GenerateResponse
import com.alexh.utils.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val GENERATION_LOGGER = LoggerFactory.getLogger(Loggers.GENERATION_ROUTING)!!

fun configureEndpointsForGeneratingPuzzles(app: Application) {
    app.routing {
        this.rateLimit(RateLimits.SUDOKU_GENERATE_NAME) {
            urls(this)
        }
    }
}

private fun urls(route: Route) {
    route.authenticate(Auths.JWT) {
        this.post(Endpoints.GENERATE) {
            val response = generateSudoku(this.call)

            handleResponse(response, call, GENERATION_LOGGER, Endpoints.GENERATE)
        }
    }
}

private suspend fun generateSudoku(call: ApplicationCall): GenerateResponse {
    val request = call.receive(GenerateRequest::class)

    val dimensionName = request.dimension
    val difficultyName = request.difficulty

    if (dimensionName.isEmpty() || difficultyName.isEmpty()) {
        return GenerateResponse.UnfilledFields
    }

    val gameNames = request.games

    val dimension = Dimension.valueOf(dimensionName)
    val difficulty = Difficulty.valueOf(difficultyName)
    val games = gameNames.map{ Game.valueOf(it) }.toSortedSet()
    val info = MakeSudokuCommand(dimension, difficulty, games)

    val sudoku = makeSudoku(info)

    return GenerateResponse.Success(sudoku)
}
