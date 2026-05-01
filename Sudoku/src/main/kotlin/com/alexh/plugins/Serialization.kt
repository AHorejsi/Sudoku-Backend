package com.alexh.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun configureSerialization(app: Application) {
    val config = app.environment.config

    val devMode = config.property("ktor.development").getString().toBoolean()
    val testMode = config.property("ktor.testing").getString().toBoolean()
    val jsonConfig =
        if (devMode || testMode)
            Json { this.prettyPrint = true }
        else
            Json

    app.install(ContentNegotiation) {
        this.json(jsonConfig)
    }
}
