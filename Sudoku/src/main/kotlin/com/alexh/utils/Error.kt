package com.alexh.utils

import com.alexh.utils.except.CookieException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.slf4j.Logger
import java.sql.SQLException

fun cookieError(cookieName: String, logger: Logger): Nothing {
    val message = "Cookie name $cookieName was not found"

    logger.error(message)
    
    throw CookieException(message)
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
            is CookieException, is NullPointerException -> HttpStatusCode.UnprocessableEntity
            else -> HttpStatusCode.InternalServerError
        }

        call.respond(status, stackTrace)
        logger.error(stackTrace)
    }
}
