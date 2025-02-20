package com.alexh.route

import com.alexh.game.*
import com.alexh.utils.Endpoints
import com.alexh.utils.Cookies
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Puzzle-Generation-Routing")

fun configureRoutingForGeneratingPuzzles(app: Application) {
    app.routing {
        this.authenticate("auth-jwt") {
            this.get(Endpoints.GENERATE) {
                runCatching {
                    generatePuzzle(this.call)
                }.onSuccess {
                    logger.info("Generated puzzle successfully")
                }.onFailure {
                    logger.error(it.stackTraceToString())
                }
            }
        }
    }
}

private suspend fun generatePuzzle(call: ApplicationCall) {
    val cookies = call.request.cookies

    val dimension = getDimension(cookies)
    val difficulty = getDifficulty(cookies)
    val games = getGames(cookies)

    val info = MakeSudokuCommand(dimension, difficulty, games)
    val sudoku = makeSudoku(info)

    call.respond(HttpStatusCode.OK, sudoku)
}

private fun getDimension(cookies: RequestCookies): Dimension =
    cookies[Cookies.DIMENSION]?.let {
        return Dimension.valueOf(it)
    } ?: error("Cookie called ${Cookies.DIMENSION} not found")

private fun getDifficulty(cookies: RequestCookies): Difficulty =
    cookies[Cookies.DIFFICULTY]?.let {
        return Difficulty.valueOf(it)
    } ?: error("Cookie called ${Cookies.DIFFICULTY} not found")

private fun getGames(cookies: RequestCookies): Set<Game> =
    cookies[Cookies.GAMES]?.run {
        val values = this.split(",")

        return if (values.firstOrNull().isNullOrEmpty())
            emptySet()
        else
            values.map{ Game.valueOf(it) }.toSortedSet()
    } ?: error("Cookie called ${Cookies.GAMES} not found")

private fun error(message: String): Nothing {
    logger.error(message)

    throw InternalError(message)
}
