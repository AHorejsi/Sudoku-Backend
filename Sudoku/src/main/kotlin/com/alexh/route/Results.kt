package com.alexh.route

import com.alexh.models.User
import com.alexh.utils.EnvironmentVariables
import com.alexh.utils.JwtClaims
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import org.slf4j.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.time.Instant
import java.util.*

private val secret = System.getenv(EnvironmentVariables.JWT_SECRET)
private val issuer = System.getenv(EnvironmentVariables.JWT_ISSUER)
private val audience = System.getenv(EnvironmentVariables.JWT_AUDIENCE)

suspend inline fun <reified TType : Any> handleResult(
    result: TType,
    call: ApplicationCall,
    logger: Logger,
    endpoint: String
) {
    call.respond(HttpStatusCode.OK, result)
    logger.info("Successful call to $endpoint")
}

fun createJwtToken(usernameOrEmail: String): String =
    "Bearer " + JWT
        .create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim(JwtClaims.USERNAME_OR_EMAIL, usernameOrEmail)
        .withExpiresAt(oneWeekFromNow())
        .sign(Algorithm.HMAC256(secret))

private fun oneWeekFromNow(): Date =
    Date(System.currentTimeMillis() + 604_800_000)

fun refreshJwtToken(user: User, jwtPayload: Payload): String? {
    val usernameOrEmail = jwtPayload.claims[JwtClaims.USERNAME_OR_EMAIL]?.asString()

    if (null === usernameOrEmail) {
        return null
    }
    if (user.username != usernameOrEmail && user.email != usernameOrEmail) {
        return null
    }
    if (jwtPayload.expiresAt >= Date.from(Instant.now())) {
        return null
    }

    return createJwtToken(usernameOrEmail)
}
