package com.alexh.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import java.sql.Statement

fun connect(app: Application): HikariDataSource {
    Class.forName("org.postgresql.Driver")

    val config = createDbConfig(app)
    val source = HikariDataSource(config)

    source.connection.use { conn ->
        val stmt = conn.createStatement()

        stmt.use {
            createDatabaseIfNeeded(it)
        }
    }

    return source
}

private fun createDbConfig(app: Application): HikariConfig {
    val dbConfig = HikariConfig()

    if (app.environment.developmentMode) {
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
    dbConfig.connectionTestQuery = "SELECT 1;"

    return dbConfig
}

private fun createDatabaseIfNeeded(stmt: Statement) {
    runCatching {
        val dbName = System.getenv(EnvironmentVariables.DB_NAME)
        
        stmt.execute("CREATE DATABASE $dbName;")
    }
}
