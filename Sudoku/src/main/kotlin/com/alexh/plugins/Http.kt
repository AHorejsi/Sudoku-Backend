package com.alexh.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun configureHttp(app: Application) {
    app.install(CORS) {
        this.allowMethod(HttpMethod.Options)
        this.allowMethod(HttpMethod.Get)
        this.allowMethod(HttpMethod.Post)
        this.allowMethod(HttpMethod.Put)
        this.allowMethod(HttpMethod.Delete)

        this.allowHeader(HttpHeaders.AccessControlAllowOrigin)
        this.allowHeader(HttpHeaders.Connection)
        this.allowHeader(HttpHeaders.Accept)
        this.allowHeader(HttpHeaders.UserAgent)
        this.allowHeader(HttpHeaders.ContentType)

        this.allowCredentials = true

        if (app.environment.developmentMode) {
            this.anyHost() // Don't do this in production!
        } else {
            this.allowHost("http://localhost:1234")
        }
    }
}