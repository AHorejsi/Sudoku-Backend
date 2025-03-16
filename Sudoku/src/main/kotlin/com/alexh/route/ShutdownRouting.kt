package com.alexh.route

import com.alexh.utils.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*

fun configureEndpointsForShutdown(app: Application) {
    val shutdown = ShutDownUrl(Endpoints.SHUTDOWN) { 0 }

    app.routing {
        this.get(Endpoints.SHUTDOWN) {
            shutdown.doShutdown(this.call)
        }
    }
}
