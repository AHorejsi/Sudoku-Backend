package com.alexh.plugins

import com.alexh.utils.Auths
import com.alexh.utils.EnvironmentVariables
import com.alexh.utils.JwtClaims
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
import org.slf4j.Logger
import java.sql.SQLException

fun configureHttp(app: Application, logger: Logger) {
    configureJwtAuthentication(app)
    configureCors(app)
    configureStatusPages(app, logger)
    configureRequestCompression(app)
}

private fun configureJwtAuthentication(app: Application) {
    app.install(Authentication) {
        this.jwt(Auths.JWT) {
            val secret = System.getenv(EnvironmentVariables.JWT_SECRET)
            val issuer = System.getenv(EnvironmentVariables.JWT_ISSUER)
            val audience = System.getenv(EnvironmentVariables.JWT_AUDIENCE)

            this.realm = System.getenv(EnvironmentVariables.JWT_REALM)
            this.verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .withClaimPresence(JwtClaims.USERNAME_OR_EMAIL)
                    .build()
            )
            this.validate { credentials ->
                val payload = credentials.payload

                val isActualIssuer = payload.issuer == issuer
                val isAllowedAudience = payload.audience.contains(audience)
                val isAllowedUsernameOrEmail = !payload.getClaim(JwtClaims.USERNAME_OR_EMAIL).asString().isNullOrBlank()

                if (isActualIssuer && isAllowedAudience && isAllowedUsernameOrEmail)
                    JWTPrincipal(credentials.payload)
                else
                    null
            }
            this.challenge { _, _ ->
                this.call.respond(HttpStatusCode.Unauthorized, "Invalid JWT Token")
            }
        }
    }
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
            val host = System.getenv(EnvironmentVariables.CLIENT_HOST)
            val port = System.getenv(EnvironmentVariables.CLIENT_PORT)
            val protocolList = listOf("http", "https")
            val subDomainList = listOf<String>()

            this.allowHost("${host}:${port}", protocolList, subDomainList)
        }
    }
}

private fun configureStatusPages(app: Application, logger: Logger) {
    app.install(StatusPages) {
        this.exception<Throwable> { call, exception ->
            logAndSendError(app, call, logger, exception, HttpStatusCode.InternalServerError)
        }

        this.exception<IllegalArgumentException> { call, exception ->
            logAndSendError(app, call, logger, exception, HttpStatusCode.BadRequest)
        }

        this.exception<ContentTransformationException> { call, exception ->
            logAndSendError(app, call, logger, exception, HttpStatusCode.BadRequest)
        }

        this.exception<SQLException> { call, exception ->
            logAndSendError(app, call, logger, exception, HttpStatusCode.BadGateway)
        }

        this.status(HttpStatusCode.Unauthorized) { call, status ->
            logAndSendError(app, call, logger, null, status)
        }
    }
}

private suspend fun logAndSendError(
    app: Application,
    call: ApplicationCall,
    logger: Logger,
    cause: Throwable?,
    statusCode: HttpStatusCode
) {
    val config = app.environment.config

    val isDevMode = config.property("ktor.development").getString().toBoolean()
    val isTestMode = config.property("ktor.testing").getString().toBoolean()

    val stackTrace = cause?.stackTraceToString()

    if (null !== stackTrace && (isDevMode || isTestMode)) {
        call.respond(statusCode, stackTrace)
    }
    else {
        call.respond(statusCode)
    }

    logger.error(stackTrace)
}

private fun configureRequestCompression(app: Application) {
    app.install(Compression) {
        this.gzip {
            this.matchContentType(ContentType.Application.Any)
            this.minimumSize(1024)
            this.priority = 1.0
        }
    }
}
