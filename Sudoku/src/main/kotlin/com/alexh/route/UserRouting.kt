package com.alexh.route

import com.alexh.models.LoginAttempt
import com.alexh.models.Puzzle
import com.alexh.models.UserService
import com.alexh.plugins.connect
import com.alexh.utils.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("User-Routing")

fun configureEndpointsForUsers(app: Application) {
    app.routing {
        this.authenticate("auth-jwt") {
            this.put(Endpoints.CREATE_USER) {
                val result = createUser(app, this.call)

                handleResult(result, this.call, logger, "Successfully created User")
            }
            this.get(Endpoints.READ_USER) {
                val result = readUser(app, this.call)

                handleResult(result, this.call, logger, "Successfully retrieved User")
            }
            this.delete(Endpoints.DELETE_USER) {
                val result = deleteUser(app, this.call)

                handleResult(result, this.call, logger, "Successfully deleted User")
            }
            this.put(Endpoints.CREATE_PUZZLE) {
                val result = createPuzzle(app, this.call)

                handleResult(result, this.call, logger, "Successfully created Puzzle")
            }
            this.put(Endpoints.UPDATE_PUZZLE) {
                val result = updatePuzzle(app, this.call)

                handleResult(result, this.call, logger, "Successfully updated Puzzle")
            }
            this.delete(Endpoints.DELETE_PUZZLE) {
                val result = deletePuzzle(app, this.call)

                handleResult(result, this.call, logger, "Successfully deleted Puzzle")
            }
        }
    }
}

private suspend fun createUser(app: Application, call: ApplicationCall): Result<Unit> = runCatching {
    val form = call.receiveParameters()

    val username = form[FormFields.USERNAME]!!
    val password = form[FormFields.PASSWORD]!!
    val email = form[FormFields.EMAIL]!!

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        service.createUser(username, password, email)
    }
}

private suspend fun readUser(app: Application, call: ApplicationCall): Result<LoginAttempt> = runCatching {
    val form = call.receiveParameters()

    val usernameOrEmail = form[FormFields.USERNAME_OR_EMAIL]!!
    val password = form[FormFields.PASSWORD]!!

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        val user = service.readUser(usernameOrEmail, password)

        return@runCatching if (null === user)
            LoginAttempt.Failure
        else
            LoginAttempt.Success(user)
    }
}

private suspend fun deleteUser(app: Application, call: ApplicationCall): Result<Unit> = runCatching {
    val cookies = call.request.cookies
    val form = call.receiveParameters()

    val userId = cookies[Cookies.USER_ID]!!.toInt()
    val usernameOrEmail = form[FormFields.USERNAME_OR_EMAIL]!!
    val password = form[FormFields.PASSWORD]!!

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        service.deleteUser(userId, usernameOrEmail, password)
    }
}

private suspend fun createPuzzle(app: Application, call: ApplicationCall): Result<Puzzle> = runCatching {
    val cookies = call.request.cookies

    val json = cookies[Cookies.JSON]!!
    val userId = cookies[Cookies.USER_ID]!!.toInt()

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        return@runCatching service.createPuzzle(json, userId)
    }
}

private suspend fun updatePuzzle(app: Application, call: ApplicationCall): Result<Unit> = runCatching {
    val cookies = call.request.cookies

    val puzzleId = cookies[Cookies.PUZZLE_ID]!!.toInt()
    val json = cookies[Cookies.JSON]!!

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        service.updatePuzzle(puzzleId, json)
    }
}

private suspend fun deletePuzzle(app: Application, call: ApplicationCall): Result<Unit> = runCatching {
    val cookies = call.request.cookies

    val userId = cookies[Cookies.USER_ID]!!.toInt()
    val puzzleId = cookies[Cookies.PUZZLE_ID]!!.toInt()

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        service.deletePuzzle(puzzleId, userId)
    }
}
