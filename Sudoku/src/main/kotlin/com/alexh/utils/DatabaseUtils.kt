package com.alexh.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import javax.sql.DataSource

fun connectToDatabase(app: Application): DataSource {
    Class.forName(SqlStrings.DB_DRIVER)

    val config = createDbConfig(app)
    val source = HikariDataSource(config)

    source.connection!!.use { conn ->
        conn.createStatement()!!.use { stmt ->
            stmt.executeUpdate(SqlStrings.CREATE_USER_TABLE)
            stmt.executeUpdate(SqlStrings.CREATE_PUZZLE_TABLE)
        }
    }

    return source
}

private fun createDbConfig(app: Application): HikariConfig {
    val dbConfig = HikariConfig()

    if (app.environment.developmentMode) { // Config for in-memory database. Only to be used for testing
        dbConfig.jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
        dbConfig.username = "root"
        dbConfig.password = ""
    }
    else {
        val appConfig = app.environment.config

        dbConfig.jdbcUrl = appConfig.property("postgres.url").getString()
        dbConfig.username = appConfig.property("postgres.username").getString()
        dbConfig.password = appConfig.property("postgres.password").getString()
        dbConfig.driverClassName = SqlStrings.DB_DRIVER
    }

    dbConfig.connectionTimeout = 10000 // ten seconds in milliseconds
    dbConfig.maximumPoolSize = 50
    dbConfig.connectionTestQuery = "SELECT 1"

    return dbConfig
}
