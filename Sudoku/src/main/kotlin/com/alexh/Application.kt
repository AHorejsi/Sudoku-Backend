package com.alexh

import io.ktor.server.netty.*
import com.alexh.plugins.*
import com.alexh.route.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

// Specified to be called in configurations
@Suppress("UNUSED")
fun Application.setupModule() {
    configureSecurity(this)
    configureSerialization(this)
    configureHttp(this)
    configureMonitoring(this)
    configureRouting(this)
}

// Specified to be called in configurations
@Suppress("UNUSED")
fun Application.endpointModule() {
    configureRoutingForGeneratingPuzzles(this)
    configureRoutingForUsers(this)
}
