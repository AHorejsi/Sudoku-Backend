package com.alexh.route

import com.alexh.game.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.random.Random

fun configureRoutingForGeneratingPuzzles(app: Application) {
    app.routing {
        this.get("/generate/{dimension}/{difficulty}/{kind}") {
            generatePuzzle(this.call)
        }
        this.get("/solved/{kind}") {
            checkSolved(this.call)
        }
    }
}

private suspend fun generatePuzzle(call: ApplicationCall) {
    val params = call.parameters
    val rand = Random.Default

    val dimensionString = params["dimension"]
    val difficultyString = params["difficulty"]
    val kindString = params["kind"]

    if (null === dimensionString || null === difficultyString || null === kindString) {
        call.respond(HttpStatusCode.BadRequest)
    }
    else {
        val game = SudokuGame.valueOf(kindString)

        when (game) {
            SudokuGame.REGULAR -> {
                val dimension = RegularDimension.valueOf(dimensionString)
                val difficulty = RegularDifficulty.valueOf(difficultyString)
                val info = RegularInfo(dimension, difficulty)

                val puzzle = RegularSudoku.make(info, rand)

                call.respond(HttpStatusCode.OK, puzzle)
            }
            SudokuGame.HYPER -> {
                val dimension = HyperDimension.valueOf(dimensionString)
                val difficulty = HyperDifficulty.valueOf(difficultyString)
                val info = HyperInfo(dimension, difficulty)

                val puzzle = HyperSudoku.make(info, rand)

                call.respond(HttpStatusCode.OK, puzzle)
            }
            else -> TODO()
        }
    }
}

private suspend fun checkSolved(call: ApplicationCall) {
    val kindString = call.parameters["kind"]

    if (null === kindString) {
        call.respond(HttpStatusCode.BadRequest)
    }
    else {
        val game = SudokuGame.valueOf(kindString)

        val result = when (game) {
            SudokuGame.REGULAR -> call.receive(RegularJson::class).solved
            SudokuGame.HYPER -> call.receive(HyperJson::class).solved
            else -> TODO()
        }

        call.respond(HttpStatusCode.OK, result)
    }
}