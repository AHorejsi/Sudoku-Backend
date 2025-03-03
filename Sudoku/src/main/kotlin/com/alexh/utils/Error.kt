package com.alexh.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.response.*
import org.slf4j.Logger
import java.sql.SQLException

fun checkJwtToken(call: ApplicationCall, intendedOperation: String) {
    val principal = call.principal<JWTPrincipal>()

    if (null === principal) {
        throw JwtException("Invalid JWT Token")
    }

    val actualOperation = principal.payload.getClaim(JwtClaims.OP_KEY).asString()

    if (actualOperation != intendedOperation) {
        throw JwtException("Invalid JWT token operation")
    }
}

fun checkShutdown(call: ApplicationCall) {
    val principal = call.principal<UserIdPrincipal>()

    if (null === principal) {
        throw RuntimeException("Invalid Shutdown")
    }
}

suspend inline fun <reified TType : Any> handleResult(
    result: Result<TType>,
    call: ApplicationCall,
    logger: Logger,
    successMessage: String
) {
    result.onSuccess { value ->
        call.respond(HttpStatusCode.OK, value)
        logger.info(successMessage)
    }.onFailure { exception ->
        val stackTrace = exception.stackTraceToString()
        val status = when (exception) {
            is SQLException -> HttpStatusCode.BadGateway
            is CookieException -> HttpStatusCode.UnprocessableEntity
            is JwtException -> HttpStatusCode.Unauthorized
            is NullPointerException, is RequestValidationException -> HttpStatusCode.BadRequest
            else -> HttpStatusCode.InternalServerError
        }

        call.respond(status, stackTrace)
        logger.error(stackTrace)
    }
}
