package com.alexh.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import java.io.BufferedWriter
import java.io.PrintWriter

fun connect(embedded: Boolean, app: Application, driver: String): HikariDataSource {
    Class.forName(driver)

    val config = createDbConfig(embedded, app, driver)
    val source = initializeDbSource(config, driver)

    return source
}

private fun createDbConfig(embedded: Boolean, app: Application, driver: String): HikariConfig {
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
    dbConfig.driverClassName = driver

    return dbConfig
}

private fun initializeDbSource(config: HikariConfig, driver: String): HikariDataSource {
    val source = HikariDataSource(config)

    source.logWriter = PrintWriter(System.out)
    source.driverClassName = driver

    return source
}
