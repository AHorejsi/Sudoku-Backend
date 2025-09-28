package com.alexh.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.slf4j.Logger

suspend inline fun <reified TType : Any> handleResult(
    result: TType,
    call: ApplicationCall,
    logger: Logger,
    successMessage: String
) {
    call.respond(HttpStatusCode.OK, result)
    logger.info(successMessage)
}