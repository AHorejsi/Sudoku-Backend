package com.alexh.route

import com.alexh.models.UserService
import com.alexh.plugins.connect
import com.alexh.utils.Cookies
import com.alexh.utils.Endpoints
import com.alexh.utils.FormFields
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun configureRoutingForUsers(app: Application) {
    app.routing {
        this.put(Endpoints.CREATE_USER) {
            createUser(app, this.call)
        }
        this.get(Endpoints.GET_USER) {
            getUser(app, this.call)
        }
        this.delete(Endpoints.DELETE_USER) {
            deleteUser(app, this.call)
        }
        this.put(Endpoints.CREATE_PUZZLE) {
            createPuzzle(app, this.call)
        }
        this.put(Endpoints.UPDATE_PUZZLE) {
            updatePuzzle(app, this.call)
        }
        this.delete(Endpoints.DELETE_PUZZLE) {
            deletePuzzle(app, this.call)
        }
    }
}

private suspend fun createUser(app: Application, call: ApplicationCall) {
    val form = call.receiveParameters()

    val username = form[FormFields.USERNAME]
    val password = form[FormFields.PASSWORD]
    val email = form[FormFields.EMAIL]

    if (username.isNullOrEmpty() || email.isNullOrEmpty() || password.isNullOrEmpty()) {
        error()
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

    val usernameOrEmail = form[FormFields.USERNAME_OR_EMAIL]
    val password = form[FormFields.PASSWORD]

    if (usernameOrEmail.isNullOrEmpty() || password.isNullOrEmpty()) {
        error()
    }

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
    val form = call.receiveParameters()

    val userId = cookies[Cookies.USER_ID]?.toInt()
    val usernameOrEmail = form[FormFields.USERNAME_OR_EMAIL]
    val password = form[FormFields.PASSWORD]

    if (null === userId || usernameOrEmail.isNullOrEmpty() || password.isNullOrEmpty()) {
        error()
    }

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        service.deleteUser(userId, usernameOrEmail, password)

        call.respond(HttpStatusCode.OK)
    }
}

private suspend fun createPuzzle(app: Application, call: ApplicationCall) {
    val cookies = call.request.cookies

    val json = cookies[Cookies.JSON]
    val userId = cookies[Cookies.USER_ID]?.toInt()

    if (json.isNullOrEmpty() || null === userId) {
        error()
    }

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        val puzzle = service.createPuzzle(json, userId)

        call.respond(HttpStatusCode.OK, puzzle)
    }
}

private suspend fun updatePuzzle(app: Application, call: ApplicationCall) {
    val cookies = call.request.cookies

    val puzzleId = cookies[Cookies.PUZZLE_ID]?.toInt()
    val json = cookies[Cookies.JSON]

    if (null === puzzleId || json.isNullOrEmpty()) {
        error()
    }

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        service.updatePuzzle(puzzleId, json)

        call.respond(HttpStatusCode.OK)
    }
}

private suspend fun deletePuzzle(app: Application, call: ApplicationCall) {
    val cookies = call.request.cookies

    val userId = cookies[Cookies.USER_ID]?.toInt()
    val puzzleId = cookies[Cookies.PUZZLE_ID]?.toInt()

    if (null == userId || null == puzzleId) {
        error()
    }

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        service.deletePuzzle(puzzleId, userId)

        call.respond(HttpStatusCode.OK)
    }
}

private fun error(): Nothing {
    throw InternalError("Invalid Request")
}
