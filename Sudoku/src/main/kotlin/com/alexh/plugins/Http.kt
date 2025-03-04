package com.alexh.plugins

import com.alexh.models.CreateUserRequest
import com.alexh.models.UpdateUserRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.requestvalidation.*

fun configureHttp(app: Application) {
    app.install(CORS) {
        this.allowMethod(HttpMethod.Options)
        this.allowMethod(HttpMethod.Get)
        this.allowMethod(HttpMethod.Put)
        this.allowMethod(HttpMethod.Post)
        this.allowMethod(HttpMethod.Delete)

        this.allowHeader(HttpHeaders.Authorization)
        this.allowHeader(HttpHeaders.AccessControlAllowOrigin)
        this.allowHeader(HttpHeaders.Cookie)
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
        } else {
            this.allowHost(app.environment.config.host)
        }
    }

    app.install(RequestValidation) {
        this.validate<CreateUserRequest> { req ->
            if (!isValidPassword(req.password)) {
                return@validate ValidationResult.Invalid("Password requirements have not been met")
            }
            if (!isValidEmail(req.email)) {
                return@validate ValidationResult.Invalid("Email format is incorrect")
            }

            return@validate ValidationResult.Valid
        }

        this.validate<UpdateUserRequest> { req ->
            if (!isValidEmail(req.newEmail)) {
                return@validate ValidationResult.Invalid("Email format is incorrect")
            }

            return@validate ValidationResult.Valid
        }
    }

    app.install(Compression) {
        this.gzip {
            this.matchContentType(ContentType.Application.Any)
            this.minimumSize(1024)
            this.priority = 1.0
        }
    }
}

private fun isValidPassword(password: String): Boolean {
    val minLength = 16

    if (password.length < minLength) {
        return false
    }

    val letterCount = password.count { it.isLetter() }
    val digitCount = password.count { it.isDigit() }
    val symbolCount = password.count { !it.isLetterOrDigit() }

    if (0 == letterCount || 0 == digitCount || 0 == symbolCount) {
        return false
    }

    return true
}

private fun isValidEmail(email: String): Boolean {
    val pattern = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"

    return pattern.toRegex().matches(email)
}
