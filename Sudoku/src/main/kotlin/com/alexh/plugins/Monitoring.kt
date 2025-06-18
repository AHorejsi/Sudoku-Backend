package com.alexh.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.Logger
import org.slf4j.event.Level
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

fun configureMonitoring(app: Application, logger: Logger) {
    app.install(CallLogging) {
        this.level = Level.INFO
        this.filter { call -> call.request.path().startsWith("/") }
        this.callIdMdc("call-id")
    }

    app.install(CallId) {
        val counter = AtomicInteger(0)
        val config = app.environment.config

        val isDev = config.property("ktor.development").getString().toBoolean()
        val isTest = config.property("ktor.testing").getString().toBoolean()

        val env = if (isDev) "DEV" else if (isTest) "TEST" else "PROD"

        this.header(HttpHeaders.XRequestId)
        this.generate { "AUTO:$env-${counter.getAndIncrement()}" }
        this.verify { callId ->
            @Suppress("ReplaceNegatedIsEmptyWithIsNotEmpty")
            !callId.isEmpty()
        }
    }

    app.environment.monitor.subscribe(ApplicationStarted) {
        logger.info("Application Started at ${findCurrentDate()}")
    }

    app.environment.monitor.subscribe(ApplicationStopping) {
        logger.info("Application Stopped at ${findCurrentDate()}")
    }

    app.environment.monitor.subscribe(ServerReady) {
        logger.info("Server ready at ${findCurrentDate()}")
    }
}

private fun findCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
    val date = Date()

    return sdf.format(date)
}