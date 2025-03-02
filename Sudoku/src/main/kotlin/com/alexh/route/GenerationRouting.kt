package com.alexh.route

import com.alexh.game.*
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
    checkJwtToken(call, JwtClaims.GENERATE_PUZZLE_VALUE)

    val request = call.receive(MakeSudokuCommand::class)
    val sudoku = makeSudoku(request)

    return@runCatching sudoku
}
