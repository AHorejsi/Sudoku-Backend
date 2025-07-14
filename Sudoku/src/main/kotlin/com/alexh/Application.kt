package com.alexh

import io.ktor.server.netty.*
import io.ktor.server.application.*
import com.alexh.plugins.*
import com.alexh.route.*
import com.alexh.utils.Loggers
import com.alexh.utils.connect
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(Loggers.MAIN_APPLICATION)

fun main(args: Array<String>) =
    EngineMain.main(args)

// Specified to be called in configurations
@Suppress("UNUSED")
fun Application.setupModule() {
    configureSerialization(this)
    configureHttp(this)
    configureMonitoring(this, logger)
}

// Specified to be called in configurations
@Suppress("UNUSED")
fun Application.endpointModule() {
    val useEmbeddedDatabase = this.environment.developmentMode
    val source = connect(useEmbeddedDatabase, this)

    configureEndpointsForGeneratingPuzzles(this)
    configureEndpointsForUsers(this, source)
    configureEndpointsForShutdown(this)
}
