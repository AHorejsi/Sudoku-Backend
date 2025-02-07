package com.alexh.models

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class UserService(private val dbConn: Connection) {
    companion object {
        const val USER_TABLE = "Users"
        const val USER_TABLE_ID = "id"
        const val USERNAME = "username"
        const val PASSWORD = "password"
        const val EMAIL = "email"

        const val PUZZLE_TABLE = "Puzzles"
        const val PUZZLE_TABLE_ID = "id"
        const val JSON = "json"
        const val USER_ID = "userId"

        private const val CREATE_USER = "INSERT INTO $USER_TABLE ($USERNAME, $PASSWORD, $EMAIL) VALUES (?, ?, ?);"
        private const val GET_USER =
                "SELECT $USERNAME, $EMAIL, GROUP_CONCAT($JSON, ';') AS $JSON FROM $USER_TABLE " +
                "WHERE ($USERNAME = ? OR $EMAIL = ?) AND $PASSWORD = ? " +
                "LEFT JOIN $PUZZLE_TABLE ON $USER_TABLE.$USER_TABLE_ID = $PUZZLE_TABLE.$USER_ID " +
                "GROUP BY $USER_TABLE_ID;"
        private const val DELETE_USER = "DELETE FROM $USER_TABLE WHERE $USERNAME = ? AND $EMAIL = ?;"
    }

    suspend fun create(username: String, password: String, email: String) = withContext(Dispatchers.IO) {
        val stmt = this@UserService.dbConn.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS)

        stmt.setString(1, username)
        stmt.setString(2, password)
        stmt.setString(3, email)

        stmt.executeUpdate()

        val keys = stmt.generatedKeys

        if (!keys.next()) {
            throw InternalError("Failed to create User")
        }
    }

    suspend fun read(usernameOrEmail: String, password: String): LoginAttempt = withContext(Dispatchers.IO) {
        val stmt = this@UserService.dbConn.prepareStatement(GET_USER)

        stmt.setString(1, usernameOrEmail)
        stmt.setString(2, usernameOrEmail)
        stmt.setString(3, password)

        val results = stmt.executeQuery()

        if (!results.next()) {
            return@withContext LoginAttempt.Failure
        }
        else {
            val user = this@UserService.makeUser(results)

            return@withContext LoginAttempt.Success(user)
        }
    }

    private fun makeUser(results: ResultSet): User {
        val username = results.getString(UserService.USERNAME)
        val email = results.getString(UserService.EMAIL)
        val puzzles = results.getString(UserService.JSON).split(";").map(::Puzzle)

        return User(username, email, puzzles)
    }

    suspend fun delete(user: User): Unit = withContext(Dispatchers.IO) {
        val stmt = this@UserService.dbConn.prepareStatement(DELETE_USER)

        stmt.setString(1, user.username)
        stmt.setString(2, user.email)

        stmt.executeUpdate()
    }
}