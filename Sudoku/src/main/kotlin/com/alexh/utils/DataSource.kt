package com.alexh.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*

fun connect(embedded: Boolean, app: Application): HikariDataSource {
    Class.forName("org.postgresql.Driver")

    val config = createDbConfig(embedded, app)
    val source = initializeDataSource(config)

    return source
}

private fun createDbConfig(embedded: Boolean, app: Application): HikariConfig {
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
        dbConfig.driverClassName = "org.postgresql.Driver"
    }

    dbConfig.connectionTimeout = 10000
    dbConfig.maximumPoolSize = 50

    return dbConfig
}

private fun initializeDataSource(config: HikariConfig): HikariDataSource {
    val source = HikariDataSource(config)

    config.driverClassName = "org.postgresql.Driver"

    return source
}
