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

        private const val CREATE_USER_TABLE =
            "CREATE TABLE IF NOT EXISTS $USER_TABLE (" +
                "$USER_TABLE_ID SERIAL PRIMARY KEY," +
                "$USERNAME TEXT UNIQUE NOT NULL," +
                "$PASSWORD TEXT NOT NULL," +
                "$EMAIL TEXT UNIQUE NOT NULL" +
            ")"
        private const val CREATE_PUZZLE_TABLE =
            "CREATE TABLE IF NOT EXISTS $PUZZLE_TABLE (" +
                "$PUZZLE_TABLE_ID SERIAL PRIMARY KEY," +
                "$JSON LONGTEXT NOT NULL," +
                "$USER_ID INT REFERENCES $USER_TABLE($USER_TABLE_ID)" +
            ")"

        private const val CREATE_USER =
            "INSERT INTO $USER_TABLE ($USERNAME, $PASSWORD, $EMAIL) " +
            "VALUES (?, ?, ?);"
        private const val GET_USER =
            "SELECT $USER_TABLE.$USER_TABLE_ID, $USERNAME, $EMAIL, $PUZZLE_TABLE.$PUZZLE_TABLE_ID, $JSON " +
            "FROM $USER_TABLE " +
            "LEFT JOIN $PUZZLE_TABLE ON $USER_TABLE.$USER_TABLE_ID = $PUZZLE_TABLE.$USER_ID " +
            "WHERE ($USERNAME = ? OR $EMAIL = ?) AND $PASSWORD = ? " +
            "GROUP BY $USER_TABLE.$USER_TABLE_ID;"
        private const val DELETE_USER =
            "DELETE FROM $USER_TABLE " +
            "WHERE $USER_TABLE_ID = ? AND ($USERNAME = ? OR $EMAIL = ?) AND $PASSWORD = ?;"

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

    init {
        this.dbConn.createStatement().use { stmt ->
            stmt.executeUpdate(CREATE_USER_TABLE)
            stmt.executeUpdate(CREATE_PUZZLE_TABLE)
        }
    }

    suspend fun createUser(username: String, password: String, email: String) = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, username)
            stmt.setString(2, password)
            stmt.setString(3, email)

            stmt.executeUpdate()

            stmt.generatedKeys.use { keys ->
                if (!keys.next()) {
                    throw InternalError("Failed to create User")
                }
            }
        }
    }

    suspend fun getUser(usernameOrEmail: String, password: String): LoginAttempt = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(GET_USER).use { stmt ->
            stmt.setString(1, usernameOrEmail)
            stmt.setString(2, usernameOrEmail)
            stmt.setString(3, password)

            stmt.executeQuery().use { results ->
                if (!results.next()) {
                    return@withContext LoginAttempt.Failure
                }
                else {
                    val user = this@UserService.makeUser(results)

                    return@withContext LoginAttempt.Success(user)
                }
            }
        }
    }

    private fun makeUser(results: ResultSet): User {
        val userId = results.getInt("$USER_TABLE.$USER_TABLE_ID")
        val username = results.getString(UserService.USERNAME)
        val email = results.getString(UserService.EMAIL)

        val puzzles = mutableListOf<Puzzle>()

        do {
            val id = results.getInt("$PUZZLE_TABLE.$PUZZLE_TABLE_ID")
            val json = results.getString(UserService.JSON)

            if (null === json) {
                break
            }

            val newPuzzle = Puzzle(id, json)

            puzzles.add(newPuzzle)
        } while (results.next())

        return User(userId, username, email, puzzles)
    }

    suspend fun deleteUser(userId: Int, usernameOrEmail: String, password: String): Unit = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(DELETE_USER).use { stmt ->
            stmt.setInt(1, userId)
            stmt.setString(2, usernameOrEmail)
            stmt.setString(3, usernameOrEmail)
            stmt.setString(4, password)

            stmt.executeUpdate()
        }
    }

    suspend fun createPuzzle(json: String, user: User): Puzzle = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(CREATE_PUZZLE, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, json)
            stmt.setInt(2, user.id)

            stmt.executeUpdate()

            stmt.generatedKeys.use { keys ->
                if (!keys.next()) {
                    throw InternalError("Failed to create Puzzle")
                }
                else {
                    val id = keys.getInt(UserService.PUZZLE_TABLE_ID)

                    return@withContext Puzzle(id, json)
                }
            }
        }
    }

    suspend fun updatePuzzle(puzzle: Puzzle): Unit = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(UPDATE_PUZZLE).use { stmt ->
            stmt.setString(1, puzzle.json)
            stmt.setInt(2, puzzle.id)

            stmt.executeUpdate()
        }
    }

    suspend fun deletePuzzle(puzzle: Puzzle): Unit = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(DELETE_PUZZLE).use { stmt ->
            stmt.setInt(1, puzzle.id)

            stmt.executeUpdate()
        }
    }
}