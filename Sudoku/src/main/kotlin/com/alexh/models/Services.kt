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
        const val PUZZLE_ID = "puzzleId"

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
                "$JSON TEXT NOT NULL," +
                "$USER_ID INT REFERENCES $USER_TABLE($USER_TABLE_ID) " +
                    "ON UPDATE CASCADE " +
                    "ON DELETE CASCADE" +
            ")"

        private const val CREATE_USER =
            "INSERT INTO $USER_TABLE ($USERNAME, $PASSWORD, $EMAIL) " +
            "VALUES (?, ?, ?);"
        private const val GET_USER =
            "SELECT $USER_TABLE.$USER_TABLE_ID AS $USER_ID, $USERNAME, $EMAIL, " +
                    "$PUZZLE_TABLE.$PUZZLE_TABLE_ID AS $PUZZLE_ID, $JSON " +
            "FROM $USER_TABLE " +
            "LEFT JOIN $PUZZLE_TABLE ON $USER_TABLE.$USER_TABLE_ID = $PUZZLE_TABLE.$USER_ID " +
            "WHERE ($USERNAME = ? OR $EMAIL = ?) AND $PASSWORD = ?;"
        private const val UPDATE_USER =
            "UPDATE $USER_TABLE " +
            "SET $USERNAME = ?, $EMAIL = ? " +
            "WHERE $USER_TABLE_ID = ? AND $USERNAME = ? AND $EMAIL = ? AND $PASSWORD = ?;"
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
    }

    init {
        this.dbConn.createStatement().use { stmt ->
            stmt.executeUpdate(CREATE_USER_TABLE)
            stmt.executeUpdate(CREATE_PUZZLE_TABLE)
        }
    }

    suspend fun createUser(username: String, password: String, email: String): CreateUserResponse = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, username)
            stmt.setString(2, password)
            stmt.setString(3, email)

            return@withContext this@UserService.doUserCreation(stmt)
        }
    }

    private fun doUserCreation(stmt: PreparedStatement): CreateUserResponse {
        runCatching {
            stmt.executeUpdate()
        }.onFailure { ex->
            return checkForExpectedUserCreationErrors(ex)
        }

        // Serves to check for errors that were not expected
        stmt.generatedKeys.use { keys ->
            if (!keys.next()) {
                throw SQLException("Failed to create User")
            }
        }

        return CreateUserResponse.Success
    }

    private fun checkForExpectedUserCreationErrors(ex: Throwable): CreateUserResponse =
        if (ex is SQLException && ex.message!!.startsWith("Unique"))
            CreateUserResponse.DuplicateFound
        else
            throw ex

    suspend fun readUser(usernameOrEmail: String, password: String): ReadUserResponse = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(GET_USER).use { stmt ->
            stmt.setString(1, usernameOrEmail)
            stmt.setString(2, usernameOrEmail)
            stmt.setString(3, password)

            stmt.executeQuery().use { results ->
                return@withContext if (results.next())
                    this@UserService.buildUser(results)
                else
                    ReadUserResponse.FailedToFind
            }
        }
    }

    private fun buildUser(results: ResultSet): ReadUserResponse {
        val user = if (results.isLast)
            this.buildUserWithPotentiallyNoPuzzles(results)
        else
            this.buildUserWithManyPuzzles(results)

        return ReadUserResponse.Success(user)
    }

    private fun buildUserWithPotentiallyNoPuzzles(results: ResultSet): User {
        val userId = results.getInt(UserService.USER_ID)
        val username = results.getString(UserService.USERNAME)
        val email = results.getString(UserService.EMAIL)
        val puzzleId = results.getInt(UserService.PUZZLE_ID)
        val json = results.getString(UserService.JSON)

        val puzzles = mutableListOf<Puzzle>()

        if (null !== json) {
            val newPuzzle = Puzzle(puzzleId, json)

            puzzles.add(newPuzzle)
        }

        return User(userId, username, email, puzzles)
    }

    private fun buildUserWithManyPuzzles(results: ResultSet): User {
        val userId = results.getInt(UserService.USER_ID)
        val username = results.getString(UserService.USERNAME)
        val email = results.getString(UserService.EMAIL)

        val puzzles = mutableListOf<Puzzle>()

        do {
            val puzzleId = results.getInt(UserService.PUZZLE_ID)
            val json = results.getString(UserService.JSON)

            val newPuzzle = Puzzle(puzzleId, json)
            puzzles.add(newPuzzle)
        } while (results.next())

        return User(userId, username, email, puzzles)
    }

    suspend fun updateUser(
        userId: Int,
        oldUsername: String,
        oldEmail: String,
        password: String,
        newUsername: String,
        newEmail: String
    ): UpdateUserResponse = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(UPDATE_USER).use { stmt ->
            stmt.setString(1, newUsername)
            stmt.setString(2, newEmail)
            stmt.setInt(3, userId)
            stmt.setString(4, oldUsername)
            stmt.setString(5, oldEmail)
            stmt.setString(6, password)

            val amountOfRowsUpdated = stmt.executeUpdate()

            return@withContext when (amountOfRowsUpdated) {
                0 -> UpdateUserResponse.FailedToFind
                1 -> UpdateUserResponse.Success(newUsername, newEmail)
                else -> throw SQLException("More than one user updated")
            }
        }
    }

    suspend fun deleteUser(userId: Int, usernameOrEmail: String, password: String): DeleteUserResponse = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(DELETE_USER).use { stmt ->
            stmt.setInt(1, userId)
            stmt.setString(2, usernameOrEmail)
            stmt.setString(3, usernameOrEmail)
            stmt.setString(4, password)

            val amountOfRowsDeleted = stmt.executeUpdate()

            return@withContext when (amountOfRowsDeleted) {
                0 -> DeleteUserResponse.FailedToFind
                1 -> DeleteUserResponse.Success
                else -> throw SQLException("More than one user deleted")
            }
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