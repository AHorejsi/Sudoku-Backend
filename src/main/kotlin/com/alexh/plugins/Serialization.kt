package com.alexh.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun configureSerialization(app: Application) {
    app.install(ContentNegotiation) {
        this.json(Json {

        })
    }
}
