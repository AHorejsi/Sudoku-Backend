package com.alexh.route

import com.alexh.utils.JwtClaims
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

fun createJwtToken(usernameOrEmail: String): String {
    val secret = System.getenv("SUDOKU_JWT_SECRET")
    val issuer = System.getenv("SUDOKU_JWT_ISSUER")
    val audience = System.getenv("SUDOKU_JWT_AUDIENCE")
    val timeDeltaInMillis = 604800000 // 1 week

    return JWT
        .create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim(JwtClaims.USERNAME_OR_EMAIL, usernameOrEmail)
        .withExpiresAt(Date(System.currentTimeMillis() + timeDeltaInMillis))
        .sign(Algorithm.HMAC256(secret))
}