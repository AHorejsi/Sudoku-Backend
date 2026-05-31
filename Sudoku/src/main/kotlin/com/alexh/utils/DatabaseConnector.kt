package com.alexh.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import javax.sql.DataSource

fun connectToDatabase(app: Application): DataSource {
    val dbConfig = HikariConfig()
    val env = app.environment

    dbConfig.connectionTimeout = 10000 // ten seconds in milliseconds
    dbConfig.maximumPoolSize = 50
    dbConfig.connectionTestQuery = "SELECT 1"

    return if (env.developmentMode)
        setDevConfig(dbConfig) // Config for in-memory database. Only to be used in DEV environment
    else
        setTestAndProdConfig(dbConfig, env.config)
}

private fun setDevConfig(config: HikariConfig): HikariDataSource {
    config.jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    config.username = "root"
    config.password = ""

    val source = HikariDataSource(config)

    // Necessary to create tables because in-memory database will be empty
    source.connection!!.use { conn ->
        conn.createStatement()!!.use { stmt ->
            stmt.executeUpdate(SqlStrings.CREATE_USER_TABLE)
            stmt.executeUpdate(SqlStrings.CREATE_PUZZLE_TABLE)
        }
    }

    return source
}

private fun setTestAndProdConfig(dbConfig: HikariConfig, appConfig: ApplicationConfig): HikariDataSource {
    Class.forName(SqlStrings.DB_DRIVER)

    dbConfig.driverClassName = SqlStrings.DB_DRIVER
    dbConfig.jdbcUrl = appConfig.property("postgres.url").getString()
    dbConfig.username = appConfig.property("postgres.username").getString()
    dbConfig.password = appConfig.property("postgres.password").getString()

    return HikariDataSource(dbConfig)
}
