package com.alexh

import io.ktor.server.netty.*
import com.alexh.plugins.*
import com.alexh.route.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

// Specified to be called in resources/application.conf
fun Application.module() {
    configureSecurity(this)
    configureSerialization(this)
    configureRoutingForGeneratingPuzzles(this)
    configureRoutingForUsers(this)
}
