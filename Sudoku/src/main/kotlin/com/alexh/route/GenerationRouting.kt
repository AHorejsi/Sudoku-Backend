package com.alexh.route

import com.alexh.game.*
import com.alexh.utils.Endpoints
import com.alexh.utils.Cookies
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun configureRoutingForGeneratingPuzzles(app: Application) {
    app.routing {
        this.get(Endpoints.GENERATION) {
            generatePuzzle(this.call)
        }
        this.get(Endpoints.SOLVED) {
            checkSolved(this.call)
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

private suspend fun checkSolved(call: ApplicationCall) {
    throw InternalError()
}

private fun getDimension(cookies: RequestCookies): Dimension {
    val dimensionName = cookies[Cookies.DIMENSION]

    if (null === dimensionName) {
        cookieError(Cookies.DIMENSION)
    }
    else {
        return Dimension.valueOf(dimensionName)
    }
}

private fun getDifficulty(cookies: RequestCookies): Difficulty {
    val difficultyName = cookies[Cookies.DIFFICULTY]

    if (null === difficultyName) {
        cookieError(Cookies.DIFFICULTY)
    }
    else {
        return Difficulty.valueOf(difficultyName)
    }
}

private fun getGames(cookies: RequestCookies): Set<Game> {
    val gameNames = cookies[Cookies.GAMES]

    return if (null === gameNames) {
        setOf()
    }
    else {
        gameNames.split(",").map{ Game.valueOf(it) }.toSet()
    }
}

private fun cookieError(cookieName: String): Nothing {
    throw InternalError("Cookie named '$cookieName' must be supplied")
}
