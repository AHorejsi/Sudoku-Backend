package com.alexh.models

import com.alexh.utils.validatePassword
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
        const val SALT = "salt"

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
                "$EMAIL TEXT UNIQUE NOT NULL," +
                "$SALT TEXT NOT NULL" +
            ");"
        private const val CREATE_PUZZLE_TABLE =
            "CREATE TABLE IF NOT EXISTS $PUZZLE_TABLE (" +
                "$PUZZLE_TABLE_ID SERIAL PRIMARY KEY," +
                "$JSON TEXT NOT NULL," +
                "$USER_ID INT REFERENCES $USER_TABLE($USER_TABLE_ID) " +
                    "ON UPDATE CASCADE " +
                    "ON DELETE CASCADE" +
            ");"

        private const val CREATE_USER =
            "INSERT INTO $USER_TABLE ($USERNAME, $PASSWORD, $EMAIL, $SALT) " +
            "VALUES (?, ?, ?, ?);"
        private const val GET_USER =
            "SELECT $USER_TABLE.$USER_TABLE_ID AS $USER_ID, $USERNAME, $EMAIL, $PASSWORD, $SALT, " +
                    "$PUZZLE_TABLE.$PUZZLE_TABLE_ID AS $PUZZLE_ID, $JSON " +
            "FROM $USER_TABLE " +
            "LEFT JOIN $PUZZLE_TABLE ON $USER_TABLE.$USER_TABLE_ID = $PUZZLE_TABLE.$USER_ID " +
            "WHERE $USERNAME = ? OR $EMAIL = ?;"
        private const val UPDATE_USER =
            "UPDATE $USER_TABLE " +
            "SET $USERNAME = ?, $EMAIL = ? " +
            "WHERE $USER_TABLE_ID = ? AND $USERNAME = ? AND $EMAIL = ?;"
        private const val DELETE_USER =
            "DELETE FROM $USER_TABLE " +
            "WHERE $USER_TABLE_ID = ?;"

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

    suspend fun createUser(
        username: String,
        password: String,
        email: String,
        salt: String
    ): CreateUserResponse = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, username)
            stmt.setString(2, password)
            stmt.setString(3, email)
            stmt.setString(4, salt)

            return@withContext this@UserService.doUserCreation(stmt)
        }
    }

    private fun doUserCreation(stmt: PreparedStatement): CreateUserResponse {
        try {
            stmt.executeUpdate()
        }
        catch (ex: SQLException) { // check for expected errors
            val result = ex.message?.startsWith("Unique") ?: false

            if (result) {
                return CreateUserResponse.DuplicateFound
            }
        }

        // Check for errors that were not expected
        stmt.generatedKeys.use { keys ->
            if (!keys.next()) {
                return CreateUserResponse.FailedToCreate
            }
        }

        return CreateUserResponse.Success
    }

    suspend fun readUser(
        usernameOrEmail: String,
        password: String
    ): ReadUserResponse = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(GET_USER).use { stmt ->
            stmt.setString(1, usernameOrEmail)
            stmt.setString(2, usernameOrEmail)

            stmt.executeQuery().use { results ->
                return@withContext if (results.next())
                    this@UserService.buildUser(results, password)
                else
                    ReadUserResponse.FailedToFind
            }
        }
    }

    private fun buildUser(results: ResultSet, providedPassword: String): ReadUserResponse {
        val databasePassword = results.getString(UserService.PASSWORD)
        val dynamicSalt = results.getString(UserService.SALT)

        if (!validatePassword(providedPassword, databasePassword, dynamicSalt)) {
            return ReadUserResponse.FailedToFind
        }

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
        newUsername: String,
        newEmail: String
    ): UpdateUserResponse = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(UPDATE_USER).use { stmt ->
            stmt.setString(1, newUsername)
            stmt.setString(2, newEmail)
            stmt.setInt(3, userId)
            stmt.setString(4, oldUsername)
            stmt.setString(5, oldEmail)

            val amountOfRowsUpdated = stmt.executeUpdate()

            return@withContext when (amountOfRowsUpdated) {
                0 -> UpdateUserResponse.FailedToFind
                1 -> UpdateUserResponse.Success
                else -> throw RuntimeException("More than one user updated")
            }
        }
    }

    suspend fun deleteUser(userId: Int): DeleteUserResponse = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(DELETE_USER).use { stmt ->
            stmt.setInt(1, userId)

            val amountOfRowsDeleted = stmt.executeUpdate()

            return@withContext when (amountOfRowsDeleted) {
                0 -> DeleteUserResponse.FailedToFind
                1 -> DeleteUserResponse.Success
                else -> throw RuntimeException("More than one user deleted")
            }
        }
    }

    suspend fun createPuzzle(json: String, userId: Int): CreatePuzzleResponse = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(CREATE_PUZZLE, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, json)
            stmt.setInt(2, userId)

            stmt.executeUpdate()

            stmt.generatedKeys.use { keys ->
                if (!keys.next()) {
                    return@withContext CreatePuzzleResponse.FailedToCreate
                }

                val id = keys.getInt(UserService.PUZZLE_TABLE_ID)
                val puzzle = Puzzle(id, json)

                return@withContext CreatePuzzleResponse.Success(puzzle)
            }
        }
    }

    suspend fun updatePuzzle(puzzleId: Int, json: String): UpdatePuzzleResponse = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(UPDATE_PUZZLE).use { stmt ->
            stmt.setString(1, json)
            stmt.setInt(2, puzzleId)

            val amountOfRowsChanged = stmt.executeUpdate()

            return@withContext when (amountOfRowsChanged) {
                0 -> UpdatePuzzleResponse.FailedToFind
                1 -> UpdatePuzzleResponse.Success
                else -> throw RuntimeException("More than one puzzle updated")
            }
        }
    }

    suspend fun deletePuzzle(puzzleId: Int): DeletePuzzleResponse = withContext(Dispatchers.IO) {
        this@UserService.dbConn.prepareStatement(DELETE_PUZZLE).use { stmt ->
            stmt.setInt(1, puzzleId)

            val amountOfRowsDeleted = stmt.executeUpdate()

            return@withContext when (amountOfRowsDeleted) {
                0 -> DeletePuzzleResponse.FailedToFind
                1 -> DeletePuzzleResponse.Success
                else -> throw RuntimeException("More than one puzzle deleted")
            }
        }
    }
}