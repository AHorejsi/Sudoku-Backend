package com.alexh.models

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.*

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
                "$USER_ID INT REFERENCES $USER_TABLE($USER_TABLE_ID) " +
                    "ON UPDATE CASCADE " +
                    "ON DELETE CASCADE" +
            ")"

        private const val CREATE_USER =
            "INSERT INTO $USER_TABLE ($USERNAME, $PASSWORD, $EMAIL) " +
            "VALUES (?, ?, ?);"
        private const val GET_USER =
            "SELECT $USER_TABLE.$USER_TABLE_ID, $USERNAME, $EMAIL, $PUZZLE_TABLE.$PUZZLE_TABLE_ID, $JSON " +
            "FROM $USER_TABLE " +
            "LEFT JOIN $PUZZLE_TABLE ON $USER_TABLE.$USER_TABLE_ID = $PUZZLE_TABLE.$USER_ID " +
            "WHERE ($USERNAME = ? OR $EMAIL = ?) AND $PASSWORD = ?;"
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
            "WHERE $PUZZLE_TABLE_ID = ? AND $USER_ID = ?;"

        private var alreadyInitialized: Boolean = false
    }

    init {
        this.initializeTables()
    }

    private fun initializeTables() {
        if (UserService.alreadyInitialized) {
            return
        }

        this.dbConn.createStatement().use { stmt ->
            stmt.executeUpdate(CREATE_USER_TABLE)
            stmt.executeUpdate(CREATE_PUZZLE_TABLE)
        }

        UserService.alreadyInitialized = true
    }

    suspend fun createUser(username: String, password: String, email: String): UserCreationAttempt = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, username)
            stmt.setString(2, password)
            stmt.setString(3, email)

            this@UserService.doUserCreation(stmt)
        }
    }

    private fun doUserCreation(stmt: PreparedStatement): UserCreationAttempt {
        runCatching {
            stmt.executeUpdate()
        }.onFailure { ex->
            val attempt = checkForExpectedUserCreationErrors(ex)

            if (null !== attempt) {
                return attempt
            }
        }

        // Serves to check for errors that were not expected
        stmt.generatedKeys.use { keys ->
            if (!keys.next()) {
                throw SQLException("Failed to create User")
            }
        }

        return UserCreationAttempt.Success
    }

    private fun checkForExpectedUserCreationErrors(ex: Throwable): UserCreationAttempt? =
        if (ex is SQLException && ex.message!!.startsWith("Unique"))
            UserCreationAttempt.DuplicateAdded
        else
            null

    suspend fun readUser(usernameOrEmail: String, password: String): User? = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(GET_USER).use { stmt ->
            stmt.setString(1, usernameOrEmail)
            stmt.setString(2, usernameOrEmail)
            stmt.setString(3, password)

            stmt.executeQuery().use { results ->
                return@withContext if (results.next())
                    this@UserService.buildUser(results)
                else
                    null
            }
        }
    }

    private fun buildUser(results: ResultSet): User {
        if (results.isLast) {
            return this.buildUserWithPotentiallyNoPuzzles(results)
        }

        val userId = results.getInt("$USER_TABLE.$USER_TABLE_ID")
        val username = results.getString(UserService.USERNAME)
        val email = results.getString(UserService.EMAIL)

        val puzzleIdField = "$PUZZLE_TABLE.$PUZZLE_TABLE_ID"
        val puzzles = mutableListOf<Puzzle>()

        do {
            val puzzleId = results.getInt(puzzleIdField)
            val json = results.getString(UserService.JSON)

            val newPuzzle = Puzzle(puzzleId, json)
            puzzles.add(newPuzzle)
        } while (results.next())

        return User(userId, username, email, puzzles)
    }

    private fun buildUserWithPotentiallyNoPuzzles(results: ResultSet): User {
        val userId = results.getInt("$USER_TABLE.$USER_TABLE_ID")
        val username = results.getString(UserService.USERNAME)
        val email = results.getString(UserService.EMAIL)
        val puzzleId = results.getInt("$PUZZLE_TABLE.$USER_TABLE_ID")
        val json = results.getString(UserService.JSON)

        val puzzles = mutableListOf<Puzzle>()

        if (null !== json) {
            val newPuzzle = Puzzle(puzzleId, json)

            puzzles.add(newPuzzle)
        }

        return User(userId, username, email, puzzles)
    }

    suspend fun deleteUser(userId: Int, usernameOrEmail: String, password: String): UserDeletionAttempt = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(DELETE_USER).use { stmt ->
            stmt.setInt(1, userId)
            stmt.setString(2, usernameOrEmail)
            stmt.setString(3, usernameOrEmail)
            stmt.setString(4, password)

            val rowsAffected = stmt.executeUpdate()

            return@withContext if (1 == rowsAffected)
                UserDeletionAttempt.Success
            else
                UserDeletionAttempt.Failure
        }
    }

    suspend fun createPuzzle(json: String, userId: Int): Puzzle = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(CREATE_PUZZLE, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, json)
            stmt.setInt(2, userId)

            stmt.executeUpdate()

            stmt.generatedKeys.use { keys ->
                if (!keys.next()) {
                    throw SQLException("Failed to create Puzzle")
                }
                else {
                    val id = keys.getInt(UserService.PUZZLE_TABLE_ID)

                    return@withContext Puzzle(id, json)
                }
            }
        }
    }

    suspend fun updatePuzzle(puzzleId: Int, json: String): Unit = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(UPDATE_PUZZLE).use { stmt ->
            stmt.setString(1, json)
            stmt.setInt(2, puzzleId)

            stmt.executeUpdate()
        }
    }

    suspend fun deletePuzzle(puzzleId: Int, userId: Int): Unit = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(DELETE_PUZZLE).use { stmt ->
            stmt.setInt(1, puzzleId)
            stmt.setInt(2, userId)

            stmt.executeUpdate()
        }
    }
}