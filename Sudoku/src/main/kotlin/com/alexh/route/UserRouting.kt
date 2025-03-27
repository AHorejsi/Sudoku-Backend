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
private const val cost = 12

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
    val request = call.receive(CreateUserRequest::class)

    if (!isValidPassword(request.password, 12) || !isValidEmail(request.email)) {
        return@runCatching CreateUserResponse.ConditionsFailed
    }

    source.connection.use {
        val salt = config.property("ktor.security.encryption.salt").getString()
        val service = UserService(it)

        val username = request.username.lowercase().trim()
        val password = request.password + salt
        val email = request.email.lowercase().trim()

        val hashedPassword = BCrypt.withDefaults().hashToString(cost, password.toCharArray())

        return@runCatching service.createUser(username, hashedPassword, email)
    }
}

private suspend fun readUser(
    source: DataSource,
    config: ApplicationConfig,
    call: ApplicationCall
): Result<ReadUserResponse> = runCatching {
    val request = call.receive(ReadUserRequest::class)

    source.connection.use {
        val service = UserService(it)
        val salt = config.property("ktor.security.encryption.salt").getString()

        val usernameOrEmail = request.usernameOrEmail.lowercase().trim()
        val password = request.password + salt

        val hashedPassword = BCrypt.withDefaults().hashToString(cost, password.toCharArray())

        return@runCatching service.readUser(usernameOrEmail, password, hashedPassword)
    }
}

private suspend fun updateUser(
    source: DataSource,
    call: ApplicationCall
): Result<UpdateUserResponse> = runCatching {
    val request = call.receive(UpdateUserRequest::class)

    source.connection.use {
        val service = UserService(it)

        val userId = request.userId
        val oldUsername = request.oldUsername
        val newUsername = request.newUsername.lowercase().trim()
        val oldEmail = request.oldEmail
        val newEmail = request.newEmail.lowercase().trim()

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
    val request = call.receive(DeleteUserRequest::class)

    source.connection.use {
        val service = UserService(it)

        val userId = request.userId

        return@runCatching service.deleteUser(userId)
    }
}

private suspend fun createPuzzle(source: DataSource, call: ApplicationCall): Result<CreatePuzzleResponse> = runCatching {
    val request = call.receive(CreatePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it)

        val json = request.json
        val userId = request.userId

        return@runCatching service.createPuzzle(json, userId)
    }
}

private suspend fun updatePuzzle(source: DataSource, call: ApplicationCall): Result<UpdatePuzzleResponse> = runCatching {
    val request = call.receive(UpdatePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it)

        val puzzleId = request.puzzleId
        val json = request.json

        return@runCatching service.updatePuzzle(puzzleId, json)
    }
}

private suspend fun deletePuzzle(source: DataSource, call: ApplicationCall): Result<DeletePuzzleResponse> = runCatching {
    val request = call.receive(DeletePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it)

        val puzzleId = request.puzzleId

        return@runCatching service.deletePuzzle(puzzleId)
    }
}
