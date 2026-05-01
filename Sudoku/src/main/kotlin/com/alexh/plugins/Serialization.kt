package com.alexh.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun configureSerialization(app: Application) {
    val jsonConfig = determineJsonConfig(app)

    app.install(ContentNegotiation) {
        this.json(jsonConfig)
    }
}

private fun determineJsonConfig(app: Application): Json {
    val config = app.environment.config

    val devMode = config.property("ktor.development").getString().toBoolean()
    val testMode = config.property("ktor.testing").getString().toBoolean()

    return if (devMode || testMode)
        Json { this.prettyPrint = true }
    else
        Json
}
