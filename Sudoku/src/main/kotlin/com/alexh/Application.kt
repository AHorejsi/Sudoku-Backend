package com.alexh

import io.ktor.server.netty.*
import io.ktor.server.application.*
import com.alexh.plugins.*
import com.alexh.route.*
import com.alexh.utils.Loggers
import com.alexh.utils.connect
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(Loggers.MAIN_APPLICATION)

fun main(args: Array<String>) {
    runCatching {
        EngineMain.main(args)
    }.onSuccess { _ ->
        logger.info("Successfully initialized server and all configurations")
    }.onFailure { exception ->
        val stackTrace = exception.stackTraceToString()

        logger.error("FAILED SERVER INITIALIZATION:\n $stackTrace")
    }
}

// Specified to be called in configurations
@Suppress("UNUSED")
fun Application.setupModule() {
    configureSerialization(this)
    configureHttp(this, logger)
    configureMonitoring(this, logger)
}

// Specified to be called in configurations
@Suppress("UNUSED")
fun Application.endpointModule() {
    val source = connect(this)

    configureEndpointsForGeneratingPuzzles(this)
    configureEndpointsForUsers(this, source)
    configureEndpointsForShutdown(this)
}
