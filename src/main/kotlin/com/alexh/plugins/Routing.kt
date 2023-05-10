package com.alexh.plugins

import com.alexh.game.*
import com.alexh.utils.boxIndex
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*

fun Application.configureRouting() =
    routing {
        get("/generate/{dimension}/{difficulty}/{kind}") {
            generatePuzzle(this.call)
        }
        get("/solved") {
            checkSolved(this.call)
        }
    }

private suspend fun generatePuzzle(call: ApplicationCall) {
    val params = call.parameters

    val dimensionString = params["dimension"]
    val difficultyString = params["difficulty"]
    val kindString = params["kind"]

    if (null === dimensionString || null === difficultyString || null === kindString) {
        call.respond(HttpStatusCode.BadRequest)
    }
    else {
        val game = SudokuGame.valueOf(kindString)

        val puzzle = when(game) {
            SudokuGame.REGULAR -> {
                val dimension = RegularDimension.valueOf(dimensionString)
                val difficulty = RegularDifficulty.valueOf(difficultyString)
                val info = RegularInfo(dimension, difficulty)

                RegularSudoku.make(info)
            }
            else -> throw InternalError()
        }

        call.respond(HttpStatusCode.OK, puzzle)
    }
}

private suspend fun checkSolved(call: ApplicationCall) {
    val puzzle = call.receive<RegularJson>()
    val table = puzzle.table
    val length = puzzle.length
    val boxRows = puzzle.boxRows
    val boxCols = puzzle.boxCols

    val rowSeen = Array(length) { 0 }
    val colSeen = Array(length) { 0 }
    val boxSeen = Array(length) { 0 }

    var result = true

    for (index in table.indices) {
        val value = table[index]

        if (null === value) {
            result = false

            break
        }

        val mask = 1 shl value

        val rowIndex = index / table.size
        if (0 == rowSeen[rowIndex] and mask) {
            rowSeen[rowIndex] = rowSeen[rowIndex] or mask
        }
        else {
            result = false

            break
        }

        val colIndex = index % table.size
        if (0 == colSeen[colIndex] and mask) {
            colSeen[colIndex] = colSeen[colIndex] or mask
        }
        else {
            result = false

            break
        }

        val boxIndex = boxIndex(rowIndex, colIndex, boxRows, boxCols)
        if (0 == boxSeen[boxIndex] and mask) {
            boxSeen[boxIndex] = boxSeen[boxIndex] or mask
        }
        else {
            result = false

            break
        }
    }

    call.respond(HttpStatusCode.OK, result)
}
