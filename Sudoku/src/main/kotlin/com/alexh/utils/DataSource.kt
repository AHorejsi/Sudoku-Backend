package com.alexh.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*

fun connect(embedded: Boolean, app: Application): HikariDataSource {
    val dbConfig = HikariConfig()

    if (embedded) {
        dbConfig.jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
        dbConfig.username = "root"
        dbConfig.password = ""
    }
    else {
        val appConfig = app.environment.config

        dbConfig.jdbcUrl = appConfig.property("postgres.url").getString()
        dbConfig.username = appConfig.property("postgres.username").getString()
        dbConfig.password = appConfig.property("postgres.password").getString()
    }

    dbConfig.connectionTimeout = 10000
    dbConfig.maximumPoolSize = 50

    return HikariDataSource(dbConfig)
}
