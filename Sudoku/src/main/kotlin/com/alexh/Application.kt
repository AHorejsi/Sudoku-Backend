package com.alexh

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.alexh.plugins.*
import com.alexh.route.configureRoutingForGeneratingPuzzles

fun main() {
    embeddedServer(Netty, port=8080, host="0.0.0.0", module=Application::module).start(true)
}

fun Application.module() {
    //configureSecurity(this)
    configureSerialization(this)
    configureRoutingForGeneratingPuzzles(this)
}
