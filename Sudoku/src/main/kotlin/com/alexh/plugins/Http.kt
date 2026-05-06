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

fun configureHttp(app: Application, logger: Logger) {
    configureCors(app)
    configureAuthentication(app, logger)
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
        }
        else {
            val host = EnvironmentVariables.CLIENT_HOST
            val port = EnvironmentVariables.CLIENT_PORT

            this.allowHost("$host:$port")
        }
    }
}

private fun configureAuthentication(app: Application, logger: Logger) {
    app.install(Authentication) {
        this.basic(Auths.BASIC) {
            val name = EnvironmentVariables.BASIC_NAME
            val pass = EnvironmentVariables.BASIC_PASS

            this.realm = EnvironmentVariables.BASIC_REALM
            this.validate { credentials ->
                val actualName = credentials.name
                val actualPass = credentials.password

                if (name == actualName && pass == actualPass)
                    UserIdPrincipal(credentials.name)
                else
                    null
            }
        }

        this.jwt(Auths.JWT) {
            val secret = EnvironmentVariables.JWT_SECRET
            val issuer = EnvironmentVariables.JWT_ISSUER
            val audience = EnvironmentVariables.JWT_AUDIENCE

            this.realm = EnvironmentVariables.JWT_REALM
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
                val isValidUsernameOrEmail = isValidUsername(usernameOrEmail) && isValidEmail(usernameOrEmail)

                if (isValidIssuer && isValidAudience && isValidUsernameOrEmail)
                    JWTPrincipal(load)
                else
                    null
            }
            this.challenge { scheme, realm ->
                logger.error("Scheme: $scheme, Realm: $realm")
                logAndSendError(this.call, logger, null, HttpStatusCode.Unauthorized)
            }
        }
    }
}

private fun configureStatusPages(app: Application, logger: Logger) {
    app.install(StatusPages) {
        this.exception<Throwable> { call, exception ->
            logAndSendError(call, logger, exception, HttpStatusCode.InternalServerError)
        }

        this.exception<IllegalArgumentException> { call, exception ->
            logAndSendError(call, logger, exception, HttpStatusCode.BadRequest)
        }

        this.exception<ContentTransformationException> { call, exception ->
            logAndSendError(call, logger, exception, HttpStatusCode.BadRequest)
        }

        this.exception<SQLException> { call, exception ->
            logAndSendError(call, logger, exception, HttpStatusCode.BadGateway)
        }
    }
}

private suspend fun logAndSendError(
    call: ApplicationCall,
    logger: Logger,
    cause: Throwable?,
    status: HttpStatusCode
) {
    withContext(Dispatchers.IO) {
        val stackTrace = cause?.stackTraceToString() ?: "No Exception"
        val message = "${status.description}: $stackTrace"

        this.launch { call.respond(status, message) }
        this.launch { logger.error(message) }
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
