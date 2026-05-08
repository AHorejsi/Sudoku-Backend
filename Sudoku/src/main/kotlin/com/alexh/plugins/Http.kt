package com.alexh.plugins

import com.alexh.utils.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import java.sql.SQLException
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.seconds

fun configureHttp(app: Application, logger: Logger) {
    configureCors(app)
    configureAuthentication(app, logger)
    configureRateLimits(app)
    configureStatusPages(app, logger)
    configureRequestCompression(app)
}

private fun configureCors(app: Application) {
    app.install(CORS) {
        this.allowMethod(HttpMethod.Options)
        this.allowMethod(HttpMethod.Get)
        this.allowMethod(HttpMethod.Put)
        this.allowMethod(HttpMethod.Post)
        this.allowMethod(HttpMethod.Delete)

        this.allowHeader(HttpHeaders.Accept)
        this.allowHeader(HttpHeaders.AcceptCharset)
        this.allowHeader(HttpHeaders.AcceptEncoding)
        this.allowHeader(HttpHeaders.Allow)
        this.allowHeader(HttpHeaders.Authorization)
        this.allowHeader(HttpHeaders.AccessControlAllowOrigin)
        this.allowHeader(HttpHeaders.Connection)
        this.allowHeader(HttpHeaders.ContentType)
        this.allowHeader(HttpHeaders.ContentLength)
        this.allowHeader(HttpHeaders.Host)
        this.allowHeader(HttpHeaders.UserAgent)
        this.allowHeader(HttpHeaders.Vary)
        this.allowHeader(HttpHeaders.XRequestId)

        this.allowCredentials = true
        this.allowSameOrigin = true

        if (app.environment.developmentMode) {
            this.anyHost() // Don't do this in production!
        } else {
            val host = EnvironmentVariables.CLIENT_HOST
            val port = EnvironmentVariables.CLIENT_PORT

            this.allowHost("$host:$port")
        }
    }
}

private fun configureAuthentication(app: Application, logger: Logger) {
    app.install(Authentication) {
        this.jwt(Auths.JWT) {
            this.realm = EnvironmentVariables.JWT_REALM

            val secret = EnvironmentVariables.JWT_SECRET
            val issuer = EnvironmentVariables.JWT_ISSUER
            val audience = EnvironmentVariables.JWT_AUDIENCE

            this.verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .withClaimPresence(JwtClaims.USERNAME_OR_EMAIL)
                    .build()
            )
            this.validate { credentials ->
                val load = credentials.payload

                val actualIssuer = load.issuer!!
                val actualAudience = load.audience!!
                val usernameOrEmail = load.getClaim(JwtClaims.USERNAME_OR_EMAIL).asString()!!

                val isValidIssuer = actualIssuer == issuer
                val isValidAudience = actualAudience.contains(audience)
                val isValidUsernameOrEmail = isValidUsername(usernameOrEmail) || isValidEmail(usernameOrEmail)

                if (isValidIssuer && isValidAudience && isValidUsernameOrEmail)
                    JWTPrincipal(load)
                else
                    null
            }
            this.challenge { scheme, realm ->
                logAndSendError(this.call, logger, HttpStatusCode.Unauthorized) {
                    logger.error("Scheme: $scheme, Realm: $realm")
                }
            }
        }
    }
}

private fun configureRateLimits(app: Application) {
    app.install(RateLimit) {
        this.register(RateLimits.SUDOKU_GENERATE_NAME) {
            val refillGap = RateLimits.SUDOKU_GENERATE_REFILL_PERIOD.seconds

            this.rateLimiter(
                limit = RateLimits.SUDOKU_GENERATE_LIMIT,
                refillPeriod = refillGap
            )
        }
    }
}

private fun configureStatusPages(app: Application, logger: Logger) {
    app.install(StatusPages) {
        this.exception<Throwable> { call, exception ->
            logAndSendError(call, logger, HttpStatusCode.InternalServerError, exception)
        }

        this.exception<IllegalArgumentException> { call, exception ->
            logAndSendError(call, logger, HttpStatusCode.BadRequest, exception)
        }

        this.exception<ContentTransformationException> { call, exception ->
            logAndSendError(call, logger, HttpStatusCode.BadRequest, exception)
        }

        this.exception<SQLException> { call, exception ->
            logAndSendError(call, logger, HttpStatusCode.BadGateway, exception)
        }

        this.status(HttpStatusCode.TooManyRequests) { call, status ->
            logAndSendError(call, logger, status)
        }
    }
}

private suspend fun logAndSendError(
    call: ApplicationCall,
    logger: Logger,
    status: HttpStatusCode,
    cause: Throwable? = null,
    extra: EmptyCallback? = null
) {
    withContext(Dispatchers.IO) {
        val stackTrace = cause?.stackTraceToString() ?: "No Exception"
        val message = "${status.description}: $stackTrace"

        this.launch { call.respond(status, message) }
        this.launch { logger.error(message) }

        if (null !== extra) {
            this.launch { extra() }
        }
    }
}

private fun configureRequestCompression(app: Application) {
    app.install(Compression) {
        this.gzip {
            this.matchContentType(ContentType.Application.Json)
            this.minimumSize(1024)
            this.priority = 1.0
        }
    }
}
