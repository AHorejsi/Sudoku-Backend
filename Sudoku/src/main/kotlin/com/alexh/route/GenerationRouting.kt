package com.alexh.route

import com.alexh.game.*
import com.alexh.utils.Endpoints
import com.alexh.utils.Cookies
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun configureRoutingForGeneratingPuzzles(app: Application) {
    app.routing {
        this.get(Endpoints.GENERATION) {
            generatePuzzle(this.call)
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
    } ?: cookieError()

private fun getDifficulty(cookies: RequestCookies): Difficulty =
    cookies[Cookies.DIFFICULTY]?.let {
        return Difficulty.valueOf(it)
    } ?: cookieError()

private fun getGames(cookies: RequestCookies): Set<Game> =
    cookies[Cookies.GAMES]?.run {
        val values = this.split(",")

        return if (values.firstOrNull().isNullOrEmpty())
            emptySet()
        else
            values.map{ Game.valueOf(it) }.toSortedSet()
    } ?: cookieError()

private fun cookieError(): Nothing {
    throw InternalError("Necessary cookies have not been supplied")
}
