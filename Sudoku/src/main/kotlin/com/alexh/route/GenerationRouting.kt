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
            this.get(Endpoints.GENERATE) {
                val result = generatePuzzle(this.call)

                handleResult(result, this.call, logger, "Successfully generated Sudoku")
            }
        }
    }
}

private fun generatePuzzle(call: ApplicationCall): Result<SudokuJson> = runCatching {
    checkJwtToken(call, "GENERATE_PUZZLE")

    val cookies = call.request.cookies

    val dimension = getDimension(cookies)
    val difficulty = getDifficulty(cookies)
    val games = getGames(cookies)

    val info = MakeSudokuCommand(dimension, difficulty, games)
    val sudoku = makeSudoku(info)

    return@runCatching sudoku
}

private fun getDimension(cookies: RequestCookies): Dimension =
    cookies[Cookies.DIMENSION]?.let {
        return Dimension.valueOf(it)
    } ?: cookieError(Cookies.DIMENSION)

private fun getDifficulty(cookies: RequestCookies): Difficulty =
    cookies[Cookies.DIFFICULTY]?.let {
        return Difficulty.valueOf(it)
    } ?: cookieError(Cookies.DIFFICULTY)

private fun getGames(cookies: RequestCookies): Set<Game> =
    cookies[Cookies.GAMES]?.run {
        val values = this.split(",")

        return if (values.firstOrNull().isNullOrEmpty())
            emptySet()
        else
            values.map{ Game.valueOf(it) }.toSortedSet()
    } ?: cookieError(Cookies.GAMES)
