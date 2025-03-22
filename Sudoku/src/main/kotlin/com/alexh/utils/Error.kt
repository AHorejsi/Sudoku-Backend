package com.alexh.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.slf4j.Logger
import java.sql.SQLException

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
            is NullPointerException -> HttpStatusCode.BadRequest
            else -> HttpStatusCode.InternalServerError
        }

        call.respond(status, stackTrace)
        logger.error(stackTrace)
    }
}
