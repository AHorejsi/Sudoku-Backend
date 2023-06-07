package com.alexh.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

class UserService(private val connection: Connection) {
    private companion object {
        const val CREATE_USER_TABLE =
            """CREATE TABLE Users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(255) NOT NULL UNIQUE,
                password VARCHAR(65535) NOT NULL
            );"""
        const val INSERT_USER = "INSERT INTO Users VALUES (?, ?);"
        const val GET_USER_BY_LOGIN = "SELECT * FROM Users WHERE username = ? AND password = ?;"
        const val UPDATE_USER_USERNAME = "UPDATE Users SET username = ? WHERE id = ?;"
        const val UPDATE_USER_PASSWORD = "UPDATE Users SET password = ? WHERE id = ?;"
        const val DELETE_USER = "DELETE FROM Users WHERE id = ?;"
    }

    init {
        val statement = this.connection.createStatement()

        statement.executeUpdate(UserService.CREATE_USER_TABLE)
    }

    suspend fun createUser(username: String, password: String): Int = withContext(Dispatchers.IO) {
        val statement =
            this@UserService.connection.prepareStatement(UserService.INSERT_USER, Statement.RETURN_GENERATED_KEYS)

        statement.setString(1, username)
        statement.setString(2, password)

        statement.executeUpdate()

        val newKeys = statement.generatedKeys

        if (newKeys.next()) {
            return@withContext newKeys.getInt(1)
        }
        else {
            throw SQLException("Unable to get the id of new user")
        }
    }

    suspend fun getUserByLogin(username: String, password: String): User? = withContext(Dispatchers.IO) {
        val statement = this@UserService.connection.prepareStatement(UserService.GET_USER_BY_LOGIN)

        statement.setString(1, username)
        statement.setString(2, password)

        val result = statement.executeQuery()

        if (result.next()) {
            val id = result.getInt("id")

            return@withContext User(id, username, password)
        }
        else {
            return@withContext null
        }
    }

    suspend fun updateUsername(id: Int, newUsername: String): Unit = withContext(Dispatchers.IO) {
        val statement = this@UserService.connection.prepareStatement(UserService.UPDATE_USER_USERNAME)

        statement.setString(1, newUsername)
        statement.setInt(2, id)

        statement.executeUpdate()
    }

    suspend fun updatePassword(id: Int, newPassword: String): Unit = withContext(Dispatchers.IO) {
        val statement = this@UserService.connection.prepareStatement(UserService.UPDATE_USER_PASSWORD)

        statement.setString(1, newPassword)
        statement.setInt(2, id)

        statement.executeUpdate()
    }

    suspend fun deleteUser(id: Int): Unit = withContext(Dispatchers.IO) {
        val statement = this@UserService.connection.prepareStatement(UserService.DELETE_USER)

        statement.setInt(1, id)

        statement.executeUpdate()
    }
}