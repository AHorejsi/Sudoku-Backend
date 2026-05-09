package com.alexh

import io.ktor.server.netty.*
import io.ktor.server.application.*
import com.alexh.plugins.*
import com.alexh.route.*
import com.alexh.utils.Loggers
import org.slf4j.LoggerFactory

private val MAIN_LOGGER = LoggerFactory.getLogger(Loggers.MAIN_APPLICATION)!!

fun main(args: Array<String>) {
    MAIN_LOGGER.info(if (args.isEmpty()) "No CLI Arguments passed" else args.joinToString())

    try {
        EngineMain.main(args)
    } catch (ex: Throwable) { // Should only happen if the configurations are not correct
        val stackTrace = ex.stackTraceToString()

        MAIN_LOGGER.error("FAILED SERVER INITIALIZATION\n$stackTrace")
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
    configureEndpointsForHealthChecks(this)
    configureEndpointsForGeneratingPuzzles(this)
    configureEndpointsForUsers(this)
    configureEndpointsForShutdown(this)
}
