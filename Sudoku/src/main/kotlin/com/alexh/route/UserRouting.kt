package com.alexh.route

import com.alexh.models.*
import com.alexh.utils.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val logger = LoggerFactory.getLogger(Loggers.USER_ROUTING)

fun configureEndpointsForUsers(app: Application, source: DataSource) {
    app.routing {
        openUrls(source, this)

        this.authenticate(Auths.JWT) {
            authenticatedUrls(source, this)
        }
    }
}

private fun openUrls(source: DataSource, route: Routing) {
    route.put(Endpoints.CREATE_USER) {
        val result = createUser(source, this.call)

        handleResult(result, this.call, logger, Endpoints.CREATE_USER)
    }
    route.post(Endpoints.READ_USER) {
        val result = readUser(source, this.call)

        handleResult(result, this.call, logger, Endpoints.READ_USER)
    }
}

private fun authenticatedUrls(source: DataSource, route: Route) {
    route.put(Endpoints.UPDATE_USER) {
        val result = updateUser(source, this.call)

        handleResult(result, this.call, logger, Endpoints.UPDATE_USER)
    }
    route.delete(Endpoints.DELETE_USER) {
        val result = deleteUser(source, this.call)

        handleResult(result, this.call, logger, Endpoints.DELETE_USER)
    }
    route.put(Endpoints.CREATE_PUZZLE) {
        val result = createPuzzle(source, this.call)

        handleResult(result, this.call, logger, Endpoints.CREATE_PUZZLE)
    }
    route.put(Endpoints.UPDATE_PUZZLE) {
        val result = updatePuzzle(source, this.call)

        handleResult(result, this.call, logger, Endpoints.UPDATE_PUZZLE)
    }
    route.delete(Endpoints.DELETE_PUZZLE) {
        val result = deletePuzzle(source, this.call)

        handleResult(result, this.call, logger, Endpoints.DELETE_PUZZLE)
    }
    route.get(Endpoints.TOKEN_LOGIN) {
        val result = tokenLogin(source, this.call)

        handleResult(result, this.call, logger, Endpoints.TOKEN_LOGIN)
    }
    route.put(Endpoints.RENEW_JWT_TOKEN) {
        val result = renewJwtToken(this.call)

        handleResult(result, this.call, logger, Endpoints.RENEW_JWT_TOKEN)
    }
}

private suspend fun createUser(source: DataSource, call: ApplicationCall): CreateUserResponse {
    val request = call.receive(CreateUserRequest::class)

    source.connection.use {
        val service = UserService(it)

        return service.createUser(request)
    }
}

private suspend fun readUser(source: DataSource, call: ApplicationCall): ReadUserResponse {
    val request = call.receive(ReadUserRequest::class)

    source.connection.use {
        val service = UserService(it)

        return service.readUserWithPassword(request)
    }
}

private suspend fun updateUser(source: DataSource, call: ApplicationCall): UpdateUserResponse {
    val request = call.receive(UpdateUserRequest::class)

    source.connection.use {
        val service = UserService(it)

        return service.updateUser(request)
    }
}

private suspend fun deleteUser(source: DataSource, call: ApplicationCall): DeleteUserResponse {
    val request = call.receive(DeleteUserRequest::class)

    source.connection.use {
        val service = UserService(it)

        return service.deleteUser(request)
    }
}

private suspend fun createPuzzle(source: DataSource, call: ApplicationCall): CreatePuzzleResponse {
    val request = call.receive(CreatePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it)

        return service.createPuzzle(request)
    }
}

private suspend fun updatePuzzle(source: DataSource, call: ApplicationCall): UpdatePuzzleResponse {
    val request = call.receive(UpdatePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it)

        return service.updatePuzzle(request)
    }
}

private suspend fun deletePuzzle(source: DataSource, call: ApplicationCall): DeletePuzzleResponse {
    val request = call.receive(DeletePuzzleRequest::class)

    source.connection.use {
        val service = UserService(it)

        return service.deletePuzzle(request)
    }
}

private suspend fun tokenLogin(source: DataSource, call: ApplicationCall): TokenLoginResponse {
    val principal = call.principal<JWTPrincipal>()!!

    source.connection.use {
        val service = UserService(it)

        return service.readUserWithToken(principal)
    }
}

private suspend fun renewJwtToken(call: ApplicationCall): RenewTokenResponse {
    val request = call.receive(RenewTokenRequest::class)
    val principal = call.principal<JWTPrincipal>()!!

    val token = refreshJwtToken(request.user, principal.payload)

    return if (null !== token)
        RenewTokenResponse.Success(token)
    else
        RenewTokenResponse.InvalidToken
}
