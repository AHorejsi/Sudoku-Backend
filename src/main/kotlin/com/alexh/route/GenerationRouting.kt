package com.alexh.route

import com.alexh.early.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.random.Random

fun configureRoutingForGeneratingPuzzles(app: Application) {
    app.routing {
        this.get("/generate/{dimension}/{difficulty}/{game}") {
            generatePuzzle(this.call)
        }
    }
}

private suspend fun generatePuzzle(call: ApplicationCall) {
    val params = call.parameters
    val rand = Random.Default

    val dimensionString = params["dimension"]
    val difficultyString = params["difficulty"]
    val kindString = params["game"]

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

                val puzzle = RegularSudoku.make(info, rand).toJson()

                call.respond(HttpStatusCode.OK, puzzle)
            }
            else -> TODO()
        }
    }
}
