package com.alexh

import io.ktor.server.netty.*
import io.ktor.server.application.*
import com.alexh.plugins.*
import com.alexh.route.*
import com.alexh.utils.Loggers
import org.slf4j.LoggerFactory

private val MAIN_LOGGER = LoggerFactory.getLogger(Loggers.MAIN_APPLICATION)!!

fun main(args: Array<String>) {
    runCatching {
        EngineMain.main(args)
    }.onFailure { exception ->
        val stackTrace = exception.stackTraceToString()

        MAIN_LOGGER.error("FAILED SERVER INITIALIZATION:\n $stackTrace")
    }
}

// Specified to be called in configurations
@Suppress("UNUSED")
fun Application.setupModule() {
    configureSerialization(this)
    configureHttp(this, MAIN_LOGGER)
    configureMonitoring(this, MAIN_LOGGER)
}

// Specified to be called in configurations
@Suppress("UNUSED")
fun Application.endpointModule() {
    configureEndpointsForGeneratingPuzzles(this)
    configureEndpointsForUsers(this)
    configureEndpointsForShutdown(this)
}
