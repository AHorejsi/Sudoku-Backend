package com.alexh.route

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.slf4j.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.util.*

suspend inline fun <reified TType : Any> handleResult(
    result: TType,
    call: ApplicationCall,
    logger: Logger,
    endpoint: String
) {
    call.respond(HttpStatusCode.OK, result)
    logger.info("Successful call to $endpoint")
}

fun createJwtToken(app: Application): String {
    val config = app.environment.config

    val secret = config.property("jwt.secret").getString()
    val issuer = config.property("jwt.issuer").getString()
    val audience = config.property("jwt.audience").getString()

    return JWT
        .create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withExpiresAt(Date(System.currentTimeMillis() + 60000))
        .sign(Algorithm.HMAC256(secret))
}