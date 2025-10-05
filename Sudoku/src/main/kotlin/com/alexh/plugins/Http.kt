package com.alexh.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.Logger
import java.sql.SQLException
import kotlin.time.Duration.Companion.seconds

fun configureHttp(app: Application, logger: Logger) {
    app.install(StatusPages) {
        this.exception<Throwable> { call, cause ->
            respondToException(app, call, logger, cause, HttpStatusCode.InternalServerError)
        }

        this.exception<SQLException> { call, cause ->
            respondToException(app, call, logger, cause, HttpStatusCode.BadGateway)
        }
    }

    app.install(CORS) {
        this.allowMethod(HttpMethod.Options)
        this.allowMethod(HttpMethod.Get)
        this.allowMethod(HttpMethod.Put)
        this.allowMethod(HttpMethod.Post)
        this.allowMethod(HttpMethod.Delete)

        this.allowHeader(HttpHeaders.Authorization)
        this.allowHeader(HttpHeaders.AccessControlAllowOrigin)
        this.allowHeader(HttpHeaders.AcceptCharset)
        this.allowHeader(HttpHeaders.Connection)
        this.allowHeader(HttpHeaders.Accept)
        this.allowHeader(HttpHeaders.AcceptEncoding)
        this.allowHeader(HttpHeaders.UserAgent)
        this.allowHeader(HttpHeaders.ContentType)
        this.allowHeader(HttpHeaders.ContentLength)
        this.allowHeader(HttpHeaders.Vary)
        this.allowHeader(HttpHeaders.Host)
        this.allowHeader(HttpHeaders.XRequestId)

        this.allowSameOrigin = true
        this.allowCredentials = true

        if (app.environment.developmentMode) {
            this.anyHost() // Don't do this in production!
        }
        else {
            this.allowHost("localhost:1234", listOf("http", "https"))
        }
    }

    app.install(Compression) {
        this.gzip {
            this.matchContentType(ContentType.Application.Any)
            this.minimumSize(1024)
            this.priority = 1.0
        }
    }

    app.install(RateLimit) {
        this.global {
            this.rateLimiter(50, 10.seconds)
        }
    }
}

private suspend fun respondToException(
    app: Application,
    call: ApplicationCall,
    logger: Logger,
    cause: Throwable,
    statusCode: HttpStatusCode
) {
    val isDevMode = app.environment.config.property("ktor.development").getString().toBoolean()
    val isTestMode = app.environment.config.property("ktor.testing").getString().toBoolean()
    
    val stackTrace = cause.stackTraceToString()

    if (isDevMode || isTestMode) {
        call.respond(statusCode, stackTrace)
    }
    else {
        call.respond(statusCode)
    }

    logger.error(stackTrace)
}
