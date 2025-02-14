package com.alexh.route

import com.alexh.models.Puzzle
import com.alexh.models.User
import com.alexh.models.UserService
import com.alexh.plugins.connect
import com.alexh.utils.Cookies
import com.alexh.utils.Endpoints
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun configureRoutingForUsers(app: Application) {
    app.routing {
        this.post(Endpoints.CREATE_USER) {
            createUser(app, this.call)
        }
        this.get(Endpoints.GET_USER) {
            getUser(app, this.call)
        }
        this.post(Endpoints.DELETE_USER) {
            deleteUser(app, this.call)
        }
        this.post(Endpoints.CREATE_PUZZLE) {
            createPuzzle(app, this.call)
        }
        this.post(Endpoints.UPDATE_PUZZLE) {
            updatePuzzle(app, this.call)
        }
        this.post(Endpoints.DELETE_PUZZLE) {
            deletePuzzle(app, this.call)
        }
    }
}

private suspend fun createUser(app: Application, call: ApplicationCall) {
    val form = call.receiveParameters()

    val username = form["username"]
    val password = form["password"]
    val email = form["email"]

    if (username.isNullOrEmpty() || email.isNullOrEmpty() || password.isNullOrEmpty()) {
        loginError()
    }

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        service.createUser(username, password, email)

        call.respond(HttpStatusCode.OK)
    }
}

private suspend fun getUser(app: Application, call: ApplicationCall) {
    val form = call.receiveParameters()

    val usernameOrEmail = form["usernameOrEmail"]
    val password = form["password"]

    if (usernameOrEmail.isNullOrEmpty() || password.isNullOrEmpty()) {
        loginError()
    }

    println(usernameOrEmail)
    println(password)

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        val login = service.getUser(usernameOrEmail, password)

        call.respond(HttpStatusCode.OK, login)
    }
}

private suspend fun deleteUser(app: Application, call: ApplicationCall) {
    val cookies = call.request.cookies

    val userId = cookies["userId"]
    val usernameOrEmail = cookies["usernameOrEmail"]
    val password = cookies["password"]

    if (userId.isNullOrEmpty() || usernameOrEmail.isNullOrEmpty() || password.isNullOrEmpty()) {
        loginError()
    }

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        service.deleteUser(userId.toInt(), usernameOrEmail, password)

        call.respond(HttpStatusCode.OK)
    }
}

private fun loginError(): Nothing {
    throw InternalError("Invalid cookies")
}

private suspend fun createPuzzle(app: Application, call: ApplicationCall) {
    val cookies = call.request.cookies

    val json = cookies[Cookies.JSON]
    val user = call.receive(User::class)

    if (json.isNullOrEmpty()) {
        throw InternalError("Invalid JSON")
    }

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        val puzzle = service.createPuzzle(json, user)

        call.respond(HttpStatusCode.OK, puzzle)
    }
}

private suspend fun updatePuzzle(app: Application, call: ApplicationCall) {
    val puzzle = call.receive(Puzzle::class)

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        service.updatePuzzle(puzzle)

        call.respond(HttpStatusCode.OK)
    }
}

private suspend fun deletePuzzle(app: Application, call: ApplicationCall) {
    val puzzle = call.receive(Puzzle::class)

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        service.deletePuzzle(puzzle)

        call.respond(HttpStatusCode.OK)
    }
}
