package com.alexh.plugins

import com.alexh.utils.currentDateString
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.Logger
import org.slf4j.event.Level
import java.util.concurrent.atomic.AtomicLong

fun configureMonitoring(app: Application, logger: Logger) {
    configureCallId(app)
    configureCallLogging(app)
    configureAppStateMonitoring(app, logger)
}

private fun configureCallId(app: Application) {
    app.install(CallId) {
        this.header(HttpHeaders.XRequestId)
        this.verify { callId -> callId.isNotEmpty() }

        val config = app.environment.config

        val isDev = config.property("ktor.development").getString().toBoolean()
        val isTest = config.property("ktor.testing").getString().toBoolean()

        val counter = AtomicLong(0)
        val env = if (isDev) "DEV" else if (isTest) "TEST" else "PROD"

        this.generate { "AUTO:$env-${counter.getAndIncrement()}" }
    }
}

private fun configureCallLogging(app: Application) {
    app.install(CallLogging) {
        this.level = Level.INFO
        this.callIdMdc("call-id")
        this.filter { call -> call.request.path().startsWith("/") }
    }
}

private fun configureAppStateMonitoring(app: Application, logger: Logger) {
    val stateMonitoring = app.environment.monitor

    stateMonitoring.subscribe(ApplicationStarted) {
        logger.info("Application Started at ${currentDateString()}")
    }

    stateMonitoring.subscribe(ApplicationStopped) {
        logger.info("Application Stopped at ${currentDateString()}")
    }

    stateMonitoring.subscribe(ServerReady) {
        logger.info("Server Ready at ${currentDateString()}")
    }
}