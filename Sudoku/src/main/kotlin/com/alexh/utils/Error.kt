package com.alexh.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.slf4j.Logger

fun doError(message: String, logger: Logger): Nothing {
    logger.error(message)
    
    throw InternalError(message)
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
        call.respond(HttpStatusCode.InternalServerError)

        logger.error(exception.stackTraceToString())
    }
}
