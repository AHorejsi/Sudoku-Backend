package com.alexh.route

import com.alexh.models.*
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

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.CREATE_USER}")
            }
            this.get(Endpoints.READ_USER) {
                val result = readUser(app, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.READ_USER}")
            }
            this.put(Endpoints.UPDATE_USER) {
                val result = updateUser(app, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.UPDATE_USER}")
            }
            this.delete(Endpoints.DELETE_USER) {
                val result = deleteUser(app, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.DELETE_USER}")
            }
            this.put(Endpoints.CREATE_PUZZLE) {
                val result = createPuzzle(app, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.CREATE_PUZZLE}")
            }
            this.put(Endpoints.UPDATE_PUZZLE) {
                val result = updatePuzzle(app, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.UPDATE_PUZZLE}")
            }
            this.delete(Endpoints.DELETE_PUZZLE) {
                val result = deletePuzzle(app, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.DELETE_PUZZLE}")
            }
        }
    }
}

private suspend fun createUser(app: Application, call: ApplicationCall): Result<CreateUserResponse> = runCatching {
    checkJwtToken(call, JwtClaims.CREATE_USER_VALUE)

    val request = call.receive(CreateUserRequest::class)

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        return@runCatching service.createUser(request.username, request.password, request.email)
    }
}

private suspend fun readUser(app: Application, call: ApplicationCall): Result<ReadUserResponse> = runCatching {
    checkJwtToken(call, JwtClaims.READ_USER_VALUE)

    val request = call.receive(ReadUserRequest::class)

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val service = UserService(it)

        return@runCatching service.readUser(request.usernameOrEmail, request.password)
    }
}

private suspend fun updateUser(app: Application, call: ApplicationCall): Result<UpdateUserResponse> = runCatching {
    checkJwtToken(call, JwtClaims.UPDATE_USER_VALUE)

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val cookies = call.request.cookies
        val request = call.receive(UpdateUserRequest::class)

        val userId = getCookie(cookies, Cookies.USER_ID).toInt()
        val oldUsername = getCookie(cookies, Cookies.USERNAME)
        val oldEmail = getCookie(cookies, Cookies.EMAIL)

        val service = UserService(it)

        return@runCatching service.updateUser(
            userId,
            oldUsername,
            oldEmail,
            request.password,
            request.newUsername,
            request.newEmail
        )
    }
}

private suspend fun deleteUser(app: Application, call: ApplicationCall): Result<DeleteUserResponse> = runCatching {
    checkJwtToken(call, JwtClaims.DELETE_USER_VALUE)

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val cookies = call.request.cookies
        val request = call.receive(DeleteUserRequest::class)

        val userId = getCookie(cookies, Cookies.USER_ID).toInt()

        val service = UserService(it)

        return@runCatching service.deleteUser(userId, request.usernameOrEmail, request.password)
    }
}

private suspend fun createPuzzle(app: Application, call: ApplicationCall): Result<Puzzle> = runCatching {
    checkJwtToken(call, JwtClaims.CREATE_PUZZLE_VALUE)

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val cookies = call.request.cookies

        val json = getCookie(cookies, Cookies.JSON)
        val userId = getCookie(cookies, Cookies.USER_ID).toInt()

        val service = UserService(it)

        return@runCatching service.createPuzzle(json, userId)
    }
}

private suspend fun updatePuzzle(app: Application, call: ApplicationCall): Result<Unit> = runCatching {
    checkJwtToken(call, JwtClaims.UPDATE_PUZZLE_VALUE)

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val cookies = call.request.cookies

        val puzzleId = getCookie(cookies, Cookies.PUZZLE_ID).toInt()
        val json = getCookie(cookies, Cookies.JSON)

        val service = UserService(it)

        service.updatePuzzle(puzzleId, json)
    }
}

private suspend fun deletePuzzle(app: Application, call: ApplicationCall): Result<Unit> = runCatching {
    checkJwtToken(call, JwtClaims.DELETE_PUZZLE_VALUE)

    val useEmbeddedDatabase = app.environment.developmentMode
    val db = connect(useEmbeddedDatabase, app)

    db.use {
        val cookies = call.request.cookies

        val userId = getCookie(cookies, Cookies.USER_ID).toInt()
        val puzzleId = getCookie(cookies, Cookies.PUZZLE_ID).toInt()

        val service = UserService(it)

        service.deletePuzzle(puzzleId, userId)
    }
}

private fun getCookie(cookies: RequestCookies, cookieName: String): String {
    val cookieValue = cookies[cookieName]

    if (null === cookieValue) {
        throw CookieException("Cookie $cookieName not found")
    }

    return cookieValue
}
