package com.alexh

import io.ktor.server.netty.*
import com.alexh.plugins.*
import com.alexh.route.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import java.io.File

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
}

// Specified to be called in configurations
@Suppress("UNUSED")
fun Application.endpointModule() {
    configureRoutingForGeneratingPuzzles(this)
    configureRoutingForUsers(this)
}
