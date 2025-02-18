package com.alexh.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun configureMonitoring(app: Application) {
    app.install(CallLogging) {
        this.level = Level.INFO
        this.filter { call -> call.request.path().startsWith("/") }
    }
}
