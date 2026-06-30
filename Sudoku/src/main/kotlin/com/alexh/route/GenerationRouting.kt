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
import io.ktor.util.pipeline.*
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

            handleResponse(response, this.call, GENERATION_LOGGER, Endpoints.GENERATE)
        }
        this.post(Endpoints.DAILY) {
            val response = getDailySudoku(this)

            handleResponse(response, this.call, GENERATION_LOGGER, Endpoints.DAILY)
        }
    }
}

private suspend fun generateSudoku(call: ApplicationCall): GenerateResponse {
    val request = call.receive(GenerateRequest::class)

    val dimensionName = request.dimension
    val difficultyName = request.difficulty
    val gameNames = request.games

    if (dimensionName.isEmpty() || difficultyName.isEmpty()) {
        return GenerateResponse.UnfilledFields
    }

    val info = makeCommand(dimensionName, difficultyName, gameNames)
    val sudoku = makeSudoku(info)

    return GenerateResponse.Success(sudoku)
}

private suspend fun getDailySudoku(scope: PipelineContext<Unit, ApplicationCall>): GenerateResponse {
    val request = scope.call.receive(GenerateRequest::class)
    val info = makeCommand(request.dimension, request.difficulty, request.games)

    val daily = retrieveDailySudoku(info, scope)
    val result = GenerateResponse.Success(daily)

    return result
}

private fun makeCommand(dimensionName: String, difficultyName: String, gameNames: Set<String>): MakeSudokuCommand {
    val dimensionResult = Dimension.valueOf(dimensionName)
    val difficultyResult = Difficulty.valueOf(difficultyName)
    val gameResult = gameNames.map(Game::valueOf).toSet()

    return MakeSudokuCommand(dimensionResult, difficultyResult, gameResult)
}
