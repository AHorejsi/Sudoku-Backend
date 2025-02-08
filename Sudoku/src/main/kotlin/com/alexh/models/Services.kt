package com.alexh.models

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

@Suppress("RemoveRedundantQualifierName")
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
        const val PUZZLE_ID = "puzzleId"

        private const val CREATE_USER =
            "INSERT INTO $USER_TABLE ($USERNAME, $PASSWORD, $EMAIL) " +
            "VALUES (?, ?, ?);"
        private const val GET_USER =
                "SELECT " +
                    "$USERNAME, $EMAIL, " +
                    "GROUP_CONCAT($PUZZLE_TABLE_ID) AS $PUZZLE_ID, " +
                    "GROUP_CONCAT($JSON, ';') AS $JSON FROM $USER_TABLE " +
                "WHERE ($USERNAME = ? OR $EMAIL = ?) AND $PASSWORD = ? " +
                "LEFT JOIN $PUZZLE_TABLE ON $USER_TABLE.$USER_TABLE_ID = $PUZZLE_TABLE.$USER_ID " +
                "GROUP BY $USER_TABLE_ID;"
        private const val DELETE_USER =
            "DELETE FROM $USER_TABLE " +
            "WHERE $USER_TABLE_ID = ? AND $USERNAME = ? AND $EMAIL = ?;"

        private const val CREATE_PUZZLE =
            "INSERT INTO $PUZZLE_TABLE ($JSON, $USER_ID)" +
            "VALUES (?, ?);"
        private const val UPDATE_PUZZLE =
            "UPDATE $PUZZLE_TABLE " +
            "SET $JSON = ? " +
            "WHERE $PUZZLE_TABLE_ID = ?;"
        private const val DELETE_PUZZLE =
            "DELETE FROM $PUZZLE_TABLE " +
            "WHERE $PUZZLE_TABLE_ID = ?;"
    }

    suspend fun createUser(username: String, password: String, email: String) = withContext(Dispatchers.IO) {
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

    suspend fun getUser(usernameOrEmail: String, password: String): LoginAttempt = withContext(Dispatchers.IO) {
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
        val userId = results.getInt(UserService.USER_TABLE_ID)
        val username = results.getString(UserService.USERNAME)
        val email = results.getString(UserService.EMAIL)

        val puzzlesIds = results.getString(UserService.PUZZLE_ID).split(";").asSequence()
        val puzzleJsons = results.getString(UserService.JSON).split(";").asSequence()
        val puzzles = puzzlesIds
            .zip(puzzleJsons)
            .map{ (id, json) -> Puzzle(id.toInt(), json) }
            .toList()

        return User(userId, username, email, puzzles)
    }

    suspend fun deleteUser(user: User): Unit = withContext(Dispatchers.IO) {
        val stmt = this@UserService.dbConn.prepareStatement(DELETE_USER)

        stmt.setInt(1, user.id)
        stmt.setString(2, user.username)
        stmt.setString(3, user.email)

        stmt.executeUpdate()
    }

    suspend fun createPuzzle(json: String, user: User): Puzzle = withContext(Dispatchers.IO) {
        val stmt = this@UserService.dbConn.prepareStatement(CREATE_PUZZLE, Statement.RETURN_GENERATED_KEYS)

        stmt.setString(1, json)
        stmt.setInt(2, user.id)

        stmt.executeUpdate()

        val keys = stmt.generatedKeys

        if (!keys.next()) {
            throw InternalError("Failed to create User")
        }
        else {
            val id = keys.getInt(UserService.PUZZLE_TABLE_ID)

            return@withContext Puzzle(id, json)
        }
    }

    suspend fun updatePuzzle(puzzle: Puzzle): Unit = withContext(Dispatchers.IO) {
        val stmt = this@UserService.dbConn.prepareStatement(UPDATE_PUZZLE)

        stmt.setString(1, puzzle.json)
        stmt.setInt(2, puzzle.id)

        stmt.executeUpdate()
    }

    suspend fun deletePuzzle(puzzle: Puzzle): Unit = withContext(Dispatchers.IO) {
        val stmt = this@UserService.dbConn.prepareStatement(DELETE_PUZZLE)

        stmt.setInt(1, puzzle.id)

        stmt.executeUpdate()
    }
}