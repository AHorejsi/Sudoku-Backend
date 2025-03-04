package com.alexh.route

import com.alexh.game.*
import com.alexh.models.GenerateRequest
import com.alexh.utils.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Puzzle-Generation-Routing")

fun configureEndpointsForGeneratingPuzzles(app: Application) {
    app.routing {
        this.authenticate("auth-jwt") {
            this.post(Endpoints.GENERATE) {
                val result = generatePuzzle(this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.GENERATE}")
            }
        }
    }
}

private suspend fun generatePuzzle(call: ApplicationCall): Result<SudokuJson> = runCatching {
    jwt(call)

    val request = call.receive(GenerateRequest::class)

    val dimension = Dimension.valueOf(request.dimension)
    val difficulty = Difficulty.valueOf(request.difficulty)
    val games = request.games.map{ Game.valueOf(it) }.toSortedSet()
    val info = MakeSudokuCommand(dimension, difficulty, games)

    val sudoku = makeSudoku(info)

    return@runCatching sudoku
}

private fun jwt(call: ApplicationCall) {
    val operations = mapOf(JwtClaims.OP_KEY to JwtClaims.GENERATE_PUZZLE_VALUE)

    checkJwtToken(call, operations)
}
