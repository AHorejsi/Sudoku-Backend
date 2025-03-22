package com.alexh.route

import at.favre.lib.crypto.bcrypt.BCrypt
import com.alexh.models.*
import com.alexh.utils.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val logger = LoggerFactory.getLogger("User-Routing")
private val cost = 12

fun configureEndpointsForUsers(app: Application, source: DataSource) {
    app.routing {
        this.put(Endpoints.CREATE_USER) {
            val result = createUser(source, app.environment.config, this.call)

            handleResult(result, this.call, logger, "Successful call to ${Endpoints.CREATE_USER}")
        }
        this.post(Endpoints.READ_USER) {
            val result = readUser(source, app.environment.config, this.call)

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

private suspend fun createUser(
    source: DataSource,
    config: ApplicationConfig,
    call: ApplicationCall
): Result<CreateUserResponse> = runCatching {
    source.connection.use {
        val service = UserService(it)
        val salt = config.property("ktor.security.encryption.salt").getString()
        val request = call.receive(CreateUserRequest::class)

        if (!request.valid) {
            return@runCatching CreateUserResponse.ConditionsFailed
        }

        val username = request.username.trim()
        val password = request.password + salt
        val email = request.email.trim()

        val hashedPassword = BCrypt.withDefaults().hashToString(cost, password.toCharArray())

        return@runCatching service.createUser(username, hashedPassword, email)
    }
}

private suspend fun readUser(
    source: DataSource,
    config: ApplicationConfig,
    call: ApplicationCall
): Result<ReadUserResponse> = runCatching {
    source.connection.use {
        val service = UserService(it)
        val request = call.receive(ReadUserRequest::class)
        val salt = config.property("ktor.security.encryption.salt").getString()

        val usernameOrEmail = request.usernameOrEmail.trim()
        val password = request.password + salt

        val hashedPassword = BCrypt.withDefaults().hashToString(cost, password.toCharArray())

        return@runCatching service.readUser(usernameOrEmail, password, hashedPassword)
    }
}

private suspend fun updateUser(
    source: DataSource,
    call: ApplicationCall
): Result<UpdateUserResponse> = runCatching {
    source.connection.use {
        val service = UserService(it)
        val request = call.receive(UpdateUserRequest::class)

        val userId = request.userId
        val oldUsername = request.oldUsername
        val newUsername = request.newUsername.trim()
        val oldEmail = request.oldEmail
        val newEmail = request.newEmail.trim()

        return@runCatching service.updateUser(
            userId,
            oldUsername,
            oldEmail,
            newUsername,
            newEmail
        )
    }
}

private suspend fun deleteUser(
    source: DataSource,
    call: ApplicationCall
): Result<DeleteUserResponse> = runCatching {
    source.connection.use {
        val service = UserService(it)
        val request = call.receive(DeleteUserRequest::class)

        val userId = request.userId

        return@runCatching service.deleteUser(userId)
    }
}

private suspend fun createPuzzle(source: DataSource, call: ApplicationCall): Result<Puzzle> = runCatching {
    source.connection.use {
        val cookies = call.request.cookies

        val json = getCookie(cookies, Cookies.JSON)
        val userId = getCookie(cookies, Cookies.USER_ID).toInt()

        val service = UserService(it)

        return@runCatching service.createPuzzle(json, userId)
    }
}

private suspend fun updatePuzzle(source: DataSource, call: ApplicationCall): Result<Unit> = runCatching {
    source.connection.use {
        val cookies = call.request.cookies

        val puzzleId = getCookie(cookies, Cookies.PUZZLE_ID).toInt()
        val json = getCookie(cookies, Cookies.JSON)

        val service = UserService(it)

        service.updatePuzzle(puzzleId, json)
    }
}

private suspend fun deletePuzzle(source: DataSource, call: ApplicationCall): Result<Unit> = runCatching {
    source.connection.use {
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
