package com.alexh.route

import com.alexh.database.PuzzleService
import java.sql.Connection
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun configurePuzzleService(app: Application, connection: Connection) {
    val service = PuzzleService(connection)
}