package com.alexh.route

import com.alexh.models.*
import com.alexh.utils.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val logger = LoggerFactory.getLogger("User-Routing")

fun configureEndpointsForUsers(app: Application, source: DataSource) {
    app.routing {
        this.authenticate("auth-jwt") {
            this.put(Endpoints.CREATE_USER) {
                val result = createUser(source, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.CREATE_USER}")
            }
            this.post(Endpoints.READ_USER) {
                val result = readUser(source, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.READ_USER}")
            }
            this.put(Endpoints.UPDATE_USER) {
                val result = updateUser(source, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.UPDATE_USER}")
            }
            this.delete(Endpoints.DELETE_USER) {
                val result = deleteUser(source, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.DELETE_USER}")
            }
            this.put(Endpoints.CREATE_PUZZLE) {
                val result = createPuzzle(source, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.CREATE_PUZZLE}")
            }
            this.put(Endpoints.UPDATE_PUZZLE) {
                val result = updatePuzzle(source, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.UPDATE_PUZZLE}")
            }
            this.delete(Endpoints.DELETE_PUZZLE) {
                val result = deletePuzzle(source, this.call)

                handleResult(result, this.call, logger, "Successful call to ${Endpoints.DELETE_PUZZLE}")
            }
        }
    }
}

private suspend fun createUser(source: DataSource, call: ApplicationCall): Result<CreateUserResponse> = runCatching {
    jwtForCreateUser(call)

    source.connection.use {
        val service = UserService(it)
        val request = call.receive(CreateUserRequest::class)

        return@runCatching service.createUser(request.username, request.password, request.email)
    }
}

private fun jwtForCreateUser(call: ApplicationCall) {
    val operations = mapOf(JwtClaims.OP_KEY to JwtClaims.CREATE_USER_VALUE)

    checkJwtToken(call, operations)
}

private suspend fun readUser(source: DataSource, call: ApplicationCall): Result<ReadUserResponse> = runCatching {
    jwtForReadUser(call)

    source.connection.use {
        val service = UserService(it)
        val request = call.receive(ReadUserRequest::class)

        return@runCatching service.readUser(request.usernameOrEmail, request.password)
    }
}

private fun jwtForReadUser(call: ApplicationCall) {
    val operations = mapOf(JwtClaims.OP_KEY to JwtClaims.READ_USER_VALUE)

    checkJwtToken(call, operations)
}

private suspend fun updateUser(source: DataSource, call: ApplicationCall): Result<UpdateUserResponse> = runCatching {
    jwtForUpdateUser(call)

    source.connection.use {
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

private fun jwtForUpdateUser(call: ApplicationCall) {
    val operations = mapOf(JwtClaims.OP_KEY to JwtClaims.UPDATE_USER_VALUE)

    checkJwtToken(call, operations)
}

private suspend fun deleteUser(source: DataSource, call: ApplicationCall): Result<DeleteUserResponse> = runCatching {
    jwtForDeleteUser(call)

    source.connection.use {
        val cookies = call.request.cookies
        val request = call.receive(DeleteUserRequest::class)

        val userId = getCookie(cookies, Cookies.USER_ID).toInt()

        val service = UserService(it)

        return@runCatching service.deleteUser(userId, request.usernameOrEmail, request.password)
    }
}

private fun jwtForDeleteUser(call: ApplicationCall) {
    val operations = mapOf(JwtClaims.OP_KEY to JwtClaims.DELETE_USER_VALUE)

    checkJwtToken(call, operations)
}

private suspend fun createPuzzle(source: DataSource, call: ApplicationCall): Result<Puzzle> = runCatching {
    jwtForCreatePuzzle(call)

    source.connection.use {
        val cookies = call.request.cookies

        val json = getCookie(cookies, Cookies.JSON)
        val userId = getCookie(cookies, Cookies.USER_ID).toInt()

        val service = UserService(it)

        return@runCatching service.createPuzzle(json, userId)
    }
}

private fun jwtForCreatePuzzle(call: ApplicationCall) {
    val operations = mapOf(JwtClaims.OP_KEY to JwtClaims.CREATE_PUZZLE_VALUE)

    checkJwtToken(call, operations)
}

private suspend fun updatePuzzle(source: DataSource, call: ApplicationCall): Result<Unit> = runCatching {
    jwtForUpdatePuzzle(call)

    source.connection.use {
        val cookies = call.request.cookies

        val puzzleId = getCookie(cookies, Cookies.PUZZLE_ID).toInt()
        val json = getCookie(cookies, Cookies.JSON)

        val service = UserService(it)

        service.updatePuzzle(puzzleId, json)
    }
}

private fun jwtForUpdatePuzzle(call: ApplicationCall) {
    val operations = mapOf(JwtClaims.OP_KEY to JwtClaims.UPDATE_PUZZLE_VALUE)

    checkJwtToken(call, operations)
}

private suspend fun deletePuzzle(source: DataSource, call: ApplicationCall): Result<Unit> = runCatching {
    jwtForDeletePuzzle(call)

    source.connection.use {
        val cookies = call.request.cookies

        val userId = getCookie(cookies, Cookies.USER_ID).toInt()
        val puzzleId = getCookie(cookies, Cookies.PUZZLE_ID).toInt()

        val service = UserService(it)

        service.deletePuzzle(puzzleId, userId)
    }
}

private fun jwtForDeletePuzzle(call: ApplicationCall) {
    val operations = mapOf(JwtClaims.OP_KEY to JwtClaims.DELETE_PUZZLE_VALUE)

    checkJwtToken(call, operations)
}

private fun getCookie(cookies: RequestCookies, cookieName: String): String {
    val cookieValue = cookies[cookieName]

    if (null === cookieValue) {
        throw CookieException("Cookie $cookieName not found")
    }

    return cookieValue
}
