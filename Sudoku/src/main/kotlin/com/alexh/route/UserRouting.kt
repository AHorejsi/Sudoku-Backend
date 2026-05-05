package com.alexh.route

import com.alexh.models.*
import com.alexh.utils.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val USER_LOGGER = LoggerFactory.getLogger(Loggers.USER_ROUTING)!!

fun configureEndpointsForUsers(app: Application) {
    val source = connectToDatabase(app)

    app.routing {
        openUrls(source, this)

        this.authenticate(Auths.JWT) {
            authenticatedUrls(source, this)
        }
    }
}

private fun openUrls(source: DataSource, route: Route) {
    route.put(Endpoints.CREATE_USER) {
        val result = createUser(source, this.call)

        handleResult(result, this.call, USER_LOGGER, Endpoints.CREATE_USER)
    }
    route.post(Endpoints.READ_USER) {
        val result = readUser(source, this.call)

        handleResult(result, this.call, USER_LOGGER, Endpoints.READ_USER)
    }
}

private fun authenticatedUrls(source: DataSource, route: Route) {
    route.put(Endpoints.UPDATE_USER) {
        val result = updateUser(source, this.call)

        handleResult(result, this.call, USER_LOGGER, Endpoints.UPDATE_USER)
    }
    route.delete(Endpoints.DELETE_USER) {
        val result = deleteUser(source, this.call)

        handleResult(result, this.call, USER_LOGGER, Endpoints.DELETE_USER)
    }
    route.put(Endpoints.CREATE_PUZZLE) {
        val result = createPuzzle(source, this.call)

        handleResult(result, this.call, USER_LOGGER, Endpoints.CREATE_PUZZLE)
    }
    route.put(Endpoints.UPDATE_PUZZLE) {
        val result = updatePuzzle(source, this.call)

        handleResult(result, this.call, USER_LOGGER, Endpoints.UPDATE_PUZZLE)
    }
    route.delete(Endpoints.DELETE_PUZZLE) {
        val result = deletePuzzle(source, this.call)

        handleResult(result, this.call, USER_LOGGER, Endpoints.DELETE_PUZZLE)
    }
    route.get(Endpoints.TOKEN_LOGIN) {
        val result = tokenLogin(source, this.call)

        handleResult(result, this.call, USER_LOGGER, Endpoints.TOKEN_LOGIN)
    }
    route.put(Endpoints.RENEW_TOKEN) {
        val result = renewJwtToken(this.call)

        handleResult(result, this.call, USER_LOGGER, Endpoints.RENEW_TOKEN)
    }
}

private suspend fun createUser(source: DataSource, call: ApplicationCall): CreateUserResponse {
    source.connection.use {
        val request = call.receive(CreateUserRequest::class)
        val response = createUser(it, request)

        return response
    }
}

private suspend fun readUser(source: DataSource, call: ApplicationCall): ReadUserResponse {
    source.connection.use {
        val request = call.receive(ReadUserRequest::class)
        val response = readUserWithPassword(it, request)

        return response
    }
}

private suspend fun tokenLogin(source: DataSource, call: ApplicationCall): TokenLoginResponse {
    source.connection.use {
        val principal = call.principal<JWTPrincipal>()!!
        val response = readUserWithToken(it, principal)

        return response
    }
}

private suspend fun updateUser(source: DataSource, call: ApplicationCall): UpdateUserResponse {
    source.connection.use {
        val request = call.receive(UpdateUserRequest::class)
        val response = updateUser(it, request)

        return response
    }
}

private suspend fun deleteUser(source: DataSource, call: ApplicationCall): DeleteUserResponse {
    source.connection.use {
        val request = call.receive(DeleteUserRequest::class)
        val response = deleteUser(it, request)

        return response
    }
}

private suspend fun createPuzzle(source: DataSource, call: ApplicationCall): CreatePuzzleResponse {
    source.connection.use {
        val request = call.receive(CreatePuzzleRequest::class)
        val response = createPuzzle(it, request)

        return response
    }
}

private suspend fun updatePuzzle(source: DataSource, call: ApplicationCall): UpdatePuzzleResponse {
    source.connection.use {
        val request = call.receive(UpdatePuzzleRequest::class)
        val response = updatePuzzle(it, request)

        return response
    }
}

private suspend fun deletePuzzle(source: DataSource, call: ApplicationCall): DeletePuzzleResponse {
    source.connection.use {
        val request = call.receive(DeletePuzzleRequest::class)
        val response = deletePuzzle(it, request)

        return response
    }
}

private suspend fun renewJwtToken(call: ApplicationCall): RenewTokenResponse {
    val principal = call.principal<JWTPrincipal>()!!
    val request = call.receive(RenewTokenRequest::class)

    val token = refreshJwtTokenIfNotExpired(request.user, principal.payload)
    val response =
        if (null !== token)
            RenewTokenResponse.Success(token)
        else
            RenewTokenResponse.InvalidToken

    return response
}
