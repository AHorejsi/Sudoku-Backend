package com.alexh.route

import com.alexh.utils.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*

fun configureEndpointsForShutdown(app: Application) {
    val shutdown = ShutDownUrl(Endpoints.SHUTDOWN) { 0 }

    app.routing {
        this.authenticate("auth-jwt") {
            this.get(Endpoints.SHUTDOWN) {
                performShutdown(app, this.call, shutdown)
            }
        }
    }
}

private suspend fun performShutdown(
    app: Application,
    call: ApplicationCall,
    shutdown: ShutDownUrl
) = runCatching {
    jwt(app, call)

    shutdown.doShutdown(call)
}

private fun jwt(app: Application, call: ApplicationCall) {
    val config = app.environment.config

    val adminUser = config.property("ktor.deployment.shutdown.adminUser").getString()
    val adminPass = config.property("ktor.deployment.shutdown.adminPassword").getString()

    val operations = mapOf(
        JwtClaims.OP_KEY to JwtClaims.SHUTDOWN_VALUE,
        JwtClaims.ADMIN_USER_KEY to adminUser,
        JwtClaims.ADMIN_PASSWORD_KEY to adminPass
    )

    checkJwtToken(call, operations)
}
