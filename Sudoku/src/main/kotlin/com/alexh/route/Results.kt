package com.alexh.route

import com.alexh.models.User
import com.alexh.utils.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import org.slf4j.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

inline fun <reified TType : Any> handleResponse(
    result: TType,
    call: ApplicationCall,
    logger: Logger,
    endpoint: String
): Unit = runBlocking(Dispatchers.IO) {
    this.launch { call.respond(HttpStatusCode.OK, result) }

    logger.info("Successful call to $endpoint")
    handleRateLimitLogging(call, logger)
}

fun createJwtToken(usernameOrEmail: String): String {
    val weekLongExpirationDate = future(604800000L) // One week in milliseconds
    val algorithm = Algorithm.HMAC256(EnvironmentVariables.JWT_SECRET)!!

    return JWT
        .create()
        .withIssuer(EnvironmentVariables.JWT_ISSUER)
        .withAudience(EnvironmentVariables.JWT_AUDIENCE)
        .withExpiresAt(weekLongExpirationDate)
        .withClaim(JwtClaims.USERNAME_OR_EMAIL, usernameOrEmail)
        .sign(algorithm)
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

fun handleRateLimitLogging(call: ApplicationCall, logger: Logger) {
    val rateLimitHeaders = getRateLimitHeaders(call)
    val message = mutableListOf<String>()

    for ((header, value) in rateLimitHeaders) {
        val actualValue = value ?: "N/A"

        message.add("$header: $actualValue")
    }

    val finalMessage = message.joinToString(", ")

    logger.info(finalMessage)
}

private fun getRateLimitHeaders(call: ApplicationCall): Map<String, String?> {
    val headers = call.response.headers

    val map = hashMapOf<String, String?>()

    map[RateLimits.LIMIT_HEADER] = headers[RateLimits.LIMIT_HEADER]
    map[RateLimits.REMAINING_HEADER] = headers[RateLimits.REMAINING_HEADER]
    map[RateLimits.RESET_HEADER] = headers[RateLimits.RESET_HEADER]
    map[RateLimits.RETRY_AFTER_HEADER] = headers[RateLimits.RETRY_AFTER_HEADER]

    return map
}
