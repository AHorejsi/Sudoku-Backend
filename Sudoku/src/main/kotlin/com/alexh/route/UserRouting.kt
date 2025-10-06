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
        openUrls(app, source, this)

        this.put(Endpoints.UPDATE_USER) {
            val result = updateUser(app, source, this.call)

            handleResult(result, this.call, logger, Endpoints.UPDATE_USER)
        }
        this.delete(Endpoints.DELETE_USER) {
            val result = deleteUser(app, source, this.call)

            handleResult(result, this.call, logger, Endpoints.DELETE_USER)
        }
        this.put(Endpoints.CREATE_PUZZLE) {
            val result = createPuzzle(app, source, this.call)

            handleResult(result, this.call, logger, Endpoints.CREATE_PUZZLE)
        }
        this.put(Endpoints.UPDATE_PUZZLE) {
            val result = updatePuzzle(app, source, this.call)

            handleResult(result, this.call, logger, Endpoints.UPDATE_PUZZLE)
        }
        this.delete(Endpoints.DELETE_PUZZLE) {
            val result = deletePuzzle(app, source, this.call)

            handleResult(result, this.call, logger, Endpoints.DELETE_PUZZLE)
        }
    }
}

private fun openUrls(app: Application, source: DataSource, routing: Routing) {
    routing.put(Endpoints.CREATE_USER) {
        val result = createUser(app, source, this.call)

        handleResult(result, this.call, logger, Endpoints.CREATE_USER)
    }
    routing.post(Endpoints.READ_USER) {
        val result = readUser(app, source, this.call)

        handleResult(result, this.call, logger, Endpoints.READ_USER)
    }
}

private suspend fun createUser(app: Application, source: DataSource, call: ApplicationCall): CreateUserResponse {
    val request = call.receive(CreateUserRequest::class)

    source.connection.use {
        val service = UserService(it, app)

        return service.createUser(request)
    }
}

private suspend fun readUser(app: Application, source: DataSource, call: ApplicationCall): ReadUserResponse {
    val request = call.receive(ReadUserRequest::class)

    source.connection.use {
        val service = UserService(it, app)

        return service.readUser(request)
    }
}

private suspend fun updateUser(app: Application, source: DataSource, call: ApplicationCall): UpdateUserResponse {
    val request = call.receive(UpdateUserRequest::class)

    source.connection.use {
        val service = UserService(it, app)

        return service.updateUser(request)
    }
}

private suspend fun deleteUser(app: Application, source: DataSource, call: ApplicationCall): DeleteUserResponse {
    val request = call.receive(DeleteUserRequest::class)

    source.connection.use {
        val service = UserService(it, app)

        return service.deleteUser(request)
    }
}

private suspend fun createPuzzle(app: Application, source: DataSource, call: ApplicationCall): CreatePuzzleResponse {
    val request = call.receive(CreatePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it, app)

        return service.createPuzzle(request)
    }
}

private suspend fun updatePuzzle(app: Application, source: DataSource, call: ApplicationCall): UpdatePuzzleResponse {
    val request = call.receive(UpdatePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it, app)

        return service.updatePuzzle(request)
    }
}

private suspend fun deletePuzzle(app: Application, source: DataSource, call: ApplicationCall): DeletePuzzleResponse {
    val request = call.receive(DeletePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it, app)

        return service.deletePuzzle(request)
    }
}
