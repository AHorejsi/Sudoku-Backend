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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

private val GENERATION_LOGGER = LoggerFactory.getLogger(Loggers.GENERATION_ROUTING)!!

fun configureEndpointsForGeneratingPuzzles(app: Application) {
    app.routing {
        this.authenticate(Auths.JWT) {
            jwtUrls(this)
        }
    }
}

private fun jwtUrls(route: Route) {
    route.rateLimit(RateLimits.SUDOKU_GENERATE_NAME) {
        this.post(Endpoints.GENERATE) {
            handleRequest(this.call)
        }
    }
}

private suspend fun handleRequest(call: ApplicationCall) {
    withContext(Dispatchers.IO) {
        this.launch { generateSudoku(call) }
        this.launch { handleRateLimitLogging(call) }
    }
}

private suspend fun generateSudoku(call: ApplicationCall) {
    val request = call.receive(GenerateRequest::class)

    val dimensionName = request.dimension
    val difficultyName = request.difficulty
    val gameNames = request.games

    val result: GenerateResponse

    if (dimensionName.isEmpty() || difficultyName.isEmpty()) {
        result = GenerateResponse.UnfilledFields
    }
    else {
        val dimension = Dimension.valueOf(dimensionName)
        val difficulty = Difficulty.valueOf(difficultyName)
        val games = gameNames.map{ Game.valueOf(it) }.toSortedSet()
        val info = MakeSudokuCommand(dimension, difficulty, games)

        val sudoku = makeSudoku(info)

        result = GenerateResponse.Success(sudoku)
    }

    handleResponse(result, call, GENERATION_LOGGER, Endpoints.GENERATE)
}

private fun handleRateLimitLogging(call: ApplicationCall) {
    val rateLimitHeaders = getRateLimitHeaders(call)
    val message = StringBuilder()

    var index = 0
    val lastIndex = rateLimitHeaders.size - 1

    for ((header, value) in rateLimitHeaders) {
        message.append(header).append(": ").append(value)

        if (lastIndex != index) {
            message.append(", ")
        }

        ++index
    }

    GENERATION_LOGGER.info(message.toString())
}

private fun getRateLimitHeaders(call: ApplicationCall): Map<String, String?> {
    val headers = call.response.headers

    val map = mutableMapOf<String, String?>()

    map[RateLimits.LIMIT_HEADER] = headers[RateLimits.LIMIT_HEADER]
    map[RateLimits.REMAINING_HEADER] = headers[RateLimits.REMAINING_HEADER]
    map[RateLimits.RESET_HEADER] = headers[RateLimits.RESET_HEADER]
    map[RateLimits.RETRY_AFTER_HEADER] = headers[RateLimits.RETRY_AFTER_HEADER]

    return map
}
