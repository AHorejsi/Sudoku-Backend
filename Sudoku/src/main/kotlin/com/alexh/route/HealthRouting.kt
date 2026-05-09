package com.alexh.route

import com.alexh.utils.Endpoints
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun configureEndpointsForHealthChecks(app: Application) {
    app.routing {
        this.get(Endpoints.PING) { this.call.respond(HttpStatusCode.OK) }
    }
}