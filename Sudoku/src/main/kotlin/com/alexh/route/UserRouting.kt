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

private const val useEmbeddedDatabase = true

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
    val cookies = call.request.cookies

    val username = cookies[Cookies.USERNAME]
    val email = cookies[Cookies.EMAIL]
    val password = cookies[Cookies.PASSWORD]

    if (username.isNullOrEmpty() || email.isNullOrEmpty() || password.isNullOrEmpty()) {
        loginError()
    }

    val db = connect(useEmbeddedDatabase, app)
    val service = UserService(db)

    service.createUser(username, password, email)

    call.respond(HttpStatusCode.OK)
}

private suspend fun getUser(app: Application, call: ApplicationCall) {
    val cookies = call.request.cookies

    val usernameOrEmail = cookies[Cookies.USERNAME_OR_EMAIL]
    val password = cookies[Cookies.PASSWORD]

    if (usernameOrEmail.isNullOrEmpty() || password.isNullOrEmpty()) {
        loginError()
    }

    val db = connect(useEmbeddedDatabase, app)
    val service = UserService(db)

    val login = service.getUser(usernameOrEmail, password)

    call.respond(HttpStatusCode.OK, login)

    call.respond(HttpStatusCode.OK)
}

private suspend fun deleteUser(app: Application, call: ApplicationCall) {
    val user = call.receive(User::class)

    val db = connect(useEmbeddedDatabase, app)
    val service = UserService(db)

    service.deleteUser(user)

    call.respond(HttpStatusCode.OK)
}

private fun loginError(): Nothing {
    throw InternalError("Invalid login information")
}

private suspend fun createPuzzle(app: Application, call: ApplicationCall) {
    val cookies = call.request.cookies

    val json = cookies[Cookies.JSON]
    val user = call.receive(User::class)

    if (json.isNullOrEmpty()) {
        throw InternalError("Invalid JSON")
    }

    val db = connect(useEmbeddedDatabase, app)
    val service = UserService(db)

    val puzzle = service.createPuzzle(json, user)

    call.respond(HttpStatusCode.OK, puzzle)
}

private suspend fun updatePuzzle(app: Application, call: ApplicationCall) {
    val puzzle = call.receive(Puzzle::class)

    val db = connect(useEmbeddedDatabase, app)
    val service = UserService(db)

    service.updatePuzzle(puzzle)

    call.respond(HttpStatusCode.OK)
}

private suspend fun deletePuzzle(app: Application, call: ApplicationCall) {
    val puzzle = call.receive(Puzzle::class)

    val db = connect(useEmbeddedDatabase, app)
    val service = UserService(db)

    service.deletePuzzle(puzzle)

    call.respond(HttpStatusCode.OK)
}
