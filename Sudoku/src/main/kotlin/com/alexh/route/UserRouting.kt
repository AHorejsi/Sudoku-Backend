package com.alexh.route

import com.alexh.models.*
import com.alexh.utils.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val logger = LoggerFactory.getLogger(Loggers.USER_ROUTING)

fun configureEndpointsForUsers(app: Application, source: DataSource) {
    app.routing {
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

private suspend fun createUser(
    source: DataSource,
    call: ApplicationCall
): Result<CreateUserResponse> = runCatching {
    val request = call.receive(CreateUserRequest::class)

    source.connection.use {
        val service = UserService(it)

        return@runCatching service.createUser(request)
    }
}

private suspend fun readUser(
    source: DataSource,
    call: ApplicationCall
): Result<ReadUserResponse> = runCatching {
    val request = call.receive(ReadUserRequest::class)

    source.connection.use {
        val service = UserService(it)

        return@runCatching service.readUser(request)
    }
}

private suspend fun updateUser(
    source: DataSource,
    call: ApplicationCall
): Result<UpdateUserResponse> = runCatching {
    val request = call.receive(UpdateUserRequest::class)

    source.connection.use {
        val service = UserService(it)

        return@runCatching service.updateUser(request)
    }
}

private suspend fun deleteUser(
    source: DataSource,
    call: ApplicationCall
): Result<DeleteUserResponse> = runCatching {
    val request = call.receive(DeleteUserRequest::class)

    source.connection.use {
        val service = UserService(it)

        return@runCatching service.deleteUser(request)
    }
}

private suspend fun createPuzzle(source: DataSource, call: ApplicationCall): Result<CreatePuzzleResponse> = runCatching {
    val request = call.receive(CreatePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it)

        return@runCatching service.createPuzzle(request)
    }
}

private suspend fun updatePuzzle(source: DataSource, call: ApplicationCall): Result<UpdatePuzzleResponse> = runCatching {
    val request = call.receive(UpdatePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it)

        return@runCatching service.updatePuzzle(request)
    }
}

private suspend fun deletePuzzle(source: DataSource, call: ApplicationCall): Result<DeletePuzzleResponse> = runCatching {
    val request = call.receive(DeletePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it)

        return@runCatching service.deletePuzzle(request)
    }
}
