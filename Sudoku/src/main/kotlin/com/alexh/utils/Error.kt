package com.alexh.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.response.*
import org.slf4j.Logger
import java.sql.SQLException

fun checkJwtToken(call: ApplicationCall, operations: Map<String, String>) {
    val principal = call.principal<JWTPrincipal>()

    if (null === principal) {
        throw JwtException("Invalid JWT Token")
    }

    for ((op, expected) in operations) {
        val actual = principal.payload.getClaim(op).asString()

        if (expected != actual) {
            throw JwtException("Invalid JWT token operation")
        }
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
