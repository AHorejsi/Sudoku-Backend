package com.alexh.route

import org. slf4j. Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend inline fun <reified TType : Any> handleResult(
    result: TType,
    call: ApplicationCall,
    logger: Logger,
    endpoint: String
) {
    call.respond(HttpStatusCode.OK, result)
    logger.info("Successful call to $endpoint")
}