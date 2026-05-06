package com.alexh.route

import com.alexh.models.User
import com.alexh.utils.EnvironmentVariables
import com.alexh.utils.JwtClaims
import com.alexh.utils.oneWeekFromNow
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import org.slf4j.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

suspend inline fun <reified TType : Any> handleResult(
    result: TType,
    call: ApplicationCall,
    logger: Logger,
    endpoint: String
) {
    withContext(Dispatchers.IO) {
        this.launch { call.respond(HttpStatusCode.OK, result) }
        this.launch { logger.info("Successful call to $endpoint") }
    }
}

fun createJwtToken(usernameOrEmail: String): String {
    val weekLongExpirationDate = oneWeekFromNow()

    return JWT
        .create()
        .withIssuer(EnvironmentVariables.JWT_ISSUER)
        .withAudience(EnvironmentVariables.JWT_AUDIENCE)
        .withExpiresAt(weekLongExpirationDate)
        .withClaim(JwtClaims.USERNAME_OR_EMAIL, usernameOrEmail)
        .sign(Algorithm.HMAC256(EnvironmentVariables.JWT_SECRET))
}

fun refreshJwtTokenIfExpired(user: User, jwtPayload: Payload): String? {
    val usernameOrEmail = jwtPayload.claims[JwtClaims.USERNAME_OR_EMAIL]?.asString()

    if (null === usernameOrEmail || user.username != usernameOrEmail && user.email != usernameOrEmail) {
        return null
    }

    val now = Date()

    if (jwtPayload.expiresAt <= now) {
        return null
    }

    return createJwtToken(usernameOrEmail)
}
