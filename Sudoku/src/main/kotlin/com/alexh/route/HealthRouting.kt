package com.alexh.route

import com.alexh.utils.Endpoints
import com.alexh.utils.Loggers
import com.alexh.utils.StatusMessages
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val HEALTH_LOGGER = LoggerFactory.getLogger(Loggers.HEALTH_ROUTING)!!

fun configureEndpointsForHealthChecks(app: Application) {
    app.routing {
        this.get(Endpoints.PING) {
            handleResponse(StatusMessages.HEALTH_CHECK, this.call, HEALTH_LOGGER, Endpoints.PING)
        }
    }
}