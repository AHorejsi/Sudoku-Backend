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
        val counter = AtomicLong(0)
        val config = app.environment.config

        val isDev = config.property("ktor.development").getString().toBoolean()
        val isTest = config.property("ktor.testing").getString().toBoolean()

        val env = if (isDev) "DEV" else if (isTest) "TEST" else "PROD"

        this.header(HttpHeaders.XRequestId)
        this.generate { "AUTO:$env-${counter.getAndIncrement()}" }
        this.verify { callId -> callId.isNotEmpty() }
    }
}

private fun configureCallLogging(app: Application) {
    app.install(CallLogging) {
        this.level = Level.INFO
        this.filter { call -> call.request.path().startsWith("/") }
        this.callIdMdc("call-id")
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