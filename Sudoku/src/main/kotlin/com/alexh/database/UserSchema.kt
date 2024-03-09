package com.alexh.database

import com.alexh.plugins.checkConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

class UserService(private val connection: Connection) {
    companion object {
        const val TABLE_NAME = "Users"
        const val PRIMARY_KEY = "id"
        const val USERNAME = "username"
        const val PASSWORD = "password"

        private const val CREATE_USER_TABLE =
            """
            CREATE TABLE ${UserService.TABLE_NAME} (
                ${UserService.PRIMARY_KEY} SERIAL PRIMARY KEY,
                ${UserService.USERNAME} VARCHAR(255) NOT NULL UNIQUE,
                ${UserService.PASSWORD} VARCHAR(255) NOT NULL
            );
            """
        private const val CREATE_USER = "INSERT INTO ${UserService.TABLE_NAME} VALUES (?, ?);"
        private const val GET_USER_BY_LOGIN = "SELECT * FROM ${UserService.TABLE_NAME} WHERE ${UserService.USERNAME} = ? AND ${UserService.PASSWORD} = ?;"
        private const val UPDATE_USER_USERNAME = "UPDATE ${UserService.TABLE_NAME} SET ${UserService.USERNAME} = ? WHERE ${UserService.PRIMARY_KEY} = ?;"
        private const val DELETE_USER = "DELETE FROM ${UserService.TABLE_NAME} WHERE ${UserService.PRIMARY_KEY} = ?;"
    }

    suspend fun createUser(username: String, password: String): Int = withContext(Dispatchers.IO) {
        checkConnection(this@UserService.connection)

        val statement =
            this@UserService.connection.prepareStatement(UserService.CREATE_USER, Statement.RETURN_GENERATED_KEYS)

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
        checkConnection(this@UserService.connection)

        val statement = this@UserService.connection.prepareStatement(UserService.GET_USER_BY_LOGIN)

        statement.setString(1, username)
        statement.setString(2, password)

        val result = statement.executeQuery()

        if (result.next()) {
            val id = result.getInt(UserService.PRIMARY_KEY)

            return@withContext User(id, username, password)
        }
        else {
            return@withContext null
        }
    }

    suspend fun updateUsername(user: User, newUsername: String): Unit = withContext(Dispatchers.IO) {
        checkConnection(this@UserService.connection)

        val statement = this@UserService.connection.prepareStatement(UserService.UPDATE_USER_USERNAME)

        statement.setString(1, newUsername)
        statement.setInt(2, user.id)

        statement.executeUpdate()
    }

    suspend fun deleteUser(user: User): Unit = withContext(Dispatchers.IO) {
        checkConnection(this@UserService.connection)

        val statement = this@UserService.connection.prepareStatement(UserService.DELETE_USER)

        statement.setInt(1, user.id)

        statement.executeUpdate()
    }
}