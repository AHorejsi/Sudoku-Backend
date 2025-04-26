package com.alexh.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun configureSerialization(app: Application) {
    val config = app.environment.config

    val devMode = config.property("ktor.development").getString().toBoolean()
    val testMode = config.property("ktor.testing").getString().toBoolean()

    if (devMode || testMode) {
        configureDevSerialization(app)
    }
    else {
        configureProdSerialization(app)
    }
}

private fun configureDevSerialization(app: Application) {
    app.install(ContentNegotiation) {
        this.json(Json { this.prettyPrint = true })
    }
}

private fun configureProdSerialization(app: Application) {
    app.install(ContentNegotiation) {
        this.json()
    }
}
