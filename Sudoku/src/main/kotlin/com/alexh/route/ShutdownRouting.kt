package com.alexh.route

import com.alexh.utils.Endpoints
import com.alexh.utils.JwtClaims
import com.alexh.utils.checkJwtToken
import com.alexh.utils.checkShutdown
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*

fun configureEndpointsForShutdown(app: Application) {
    val shutdown = ShutDownUrl(Endpoints.SHUTDOWN) { 0 }

    app.routing {
        this.authenticate("auth-shutdown", "auth-jwt") {
            this.get(Endpoints.SHUTDOWN) {
                performShutdown(this.call, shutdown)
            }
        }
    }
}

private suspend fun performShutdown(call: ApplicationCall, shutdown: ShutDownUrl) = runCatching {
    checkJwtToken(call, JwtClaims.SHUTDOWN_VALUE)
    checkShutdown(call)

    shutdown.doShutdown(call)
}
