package com.alexh.models

import com.alexh.route.createJwtToken
import com.alexh.route.refreshJwtToken
import com.alexh.utils.*
import io.ktor.server.auth.jwt.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.*

@Suppress("RemoveRedundantQualifierName")
class UserService(private val dbConn: Connection) {
    companion object {
        var initialized: Boolean = false

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
                    "STRING_AGG(CAST($PUZZLE_TABLE.$PUZZLE_TABLE_ID AS TEXT), '|') AS $PUZZLE_ID," +
                    "STRING_AGG($JSON, '|') as $JSON " +
            "FROM $USER_TABLE " +
            "LEFT JOIN $PUZZLE_TABLE ON $USER_TABLE.$USER_TABLE_ID = $PUZZLE_TABLE.$USER_ID " +
            "WHERE LOWER($USERNAME) = LOWER(?) OR LOWER($EMAIL) = LOWER(?)" +
            "GROUP BY $USER_TABLE.$USER_TABLE_ID;"
        private const val UPDATE_USER =
            "UPDATE $USER_TABLE " +
            "SET $USERNAME = ?, $EMAIL = ? " +
            "WHERE $USER_TABLE_ID = ?;"
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
        if (!UserService.initialized) {
            this.dbConn.createStatement().use { stmt ->
                stmt.executeUpdate(CREATE_USER_TABLE)
                stmt.executeUpdate(CREATE_PUZZLE_TABLE)
            }

            UserService.initialized = true
        }
    }

    suspend fun createUser(request: CreateUserRequest): CreateUserResponse = withContext(Dispatchers.IO) {
        val username = request.username.trim()
        val password = request.password
        val email = request.email.trim()

        if (username.isEmpty()) {
            return@withContext CreateUserResponse.InvalidUsername
        }
        if (!isValidPassword(password)) {
            return@withContext CreateUserResponse.InvalidPassword
        }
        if (!isValidEmail(email)) {
            return@withContext CreateUserResponse.InvalidEmail
        }

        val (passwordHash, salt) = createPassword(password)

        this@UserService.dbConn.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, username)
            stmt.setString(2, passwordHash)
            stmt.setString(3, email)
            stmt.setString(4, salt)

            return@withContext this@UserService.doUserCreation(stmt)
        }
    }

    private fun doUserCreation(stmt: PreparedStatement): CreateUserResponse {
        runCatching {
            stmt.executeUpdate()
        }.onFailure { ex ->
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

    suspend fun readUserWithPassword(request: ReadUserRequest): ReadUserResponse = withContext(Dispatchers.IO) {
        val usernameOrEmail = request.usernameOrEmail.trim()
        val password = request.password

        this@UserService.dbConn.prepareStatement(GET_USER).use { stmt ->
            stmt.setString(1, usernameOrEmail)
            stmt.setString(2, usernameOrEmail)

            stmt.executeQuery().use { results ->
                val user = this@UserService.buildUserWithPassword(results, password)

                if (null === user) {
                    return@withContext ReadUserResponse.FailedToFind
                }
                else {
                    val token = createJwtToken(usernameOrEmail)

                    return@withContext ReadUserResponse.Success(user, token)
                }
            }
        }
    }

    private fun buildUserWithPassword(results: ResultSet, providedPassword: String): User? {
        val user = this.buildUserHelper(results)

        if (null !== user) {
            val databasePassword = results.getString(UserService.PASSWORD)
            val dynamicSalt = results.getString(UserService.SALT)

            if (!validatePassword(providedPassword, databasePassword, dynamicSalt)) {
                return null
            }
        }

        return user
    }

    suspend fun readUserWithToken(principal: JWTPrincipal): TokenLoginResponse = withContext(Dispatchers.IO) {
        val usernameOrEmail = principal.payload.claims.getValue(JwtClaims.USERNAME_OR_EMAIL).asString()

        this@UserService.dbConn.prepareStatement(GET_USER).use { stmt ->
            stmt.setString(1, usernameOrEmail)
            stmt.setString(2, usernameOrEmail)

            stmt.executeQuery().use { results ->
                val user = this@UserService.buildUserHelper(results)

                if (null === user) {
                    return@withContext TokenLoginResponse.InvalidUsernameOrEmail
                }

                val newToken = refreshJwtToken(user, principal.payload)

                return@withContext if (null !== newToken)
                    TokenLoginResponse.Success(user, newToken)
                else
                    TokenLoginResponse.Expired
            }
        }
    }

    private fun buildUserHelper(results: ResultSet): User? {
        if (!results.next()) {
            return null
        }

        val userId = results.getInt(UserService.USER_ID)
        val username = results.getString(UserService.USERNAME)
        val email = results.getString(UserService.EMAIL)
        val puzzleIds = results.getString(UserService.PUZZLE_ID)?.split('|')
        val puzzleJsons = results.getString(UserService.JSON)?.split('|')

        val puzzles = mutableListOf<Puzzle>()

        if (null !== puzzleIds && null !== puzzleJsons) {
            for ((id, json) in puzzleIds.zip(puzzleJsons)) {
                val saved = Puzzle(id.toInt(), json)

                puzzles.add(saved)
            }
        }

        return User(userId, username, email, puzzles)
    }

    suspend fun updateUser(request: UpdateUserRequest): UpdateUserResponse = withContext(Dispatchers.IO) {
        val userId = request.userId
        val newUsername = request.newUsername.trim()
        val newEmail = request.newEmail.trim()

        if (newUsername.isEmpty()) {
            return@withContext UpdateUserResponse.InvalidUsername
        }
        if (!isValidEmail(newEmail)) {
            return@withContext UpdateUserResponse.InvalidEmail
        }

        this@UserService.dbConn.prepareStatement(UPDATE_USER).use { stmt ->
            stmt.setString(1, newUsername)
            stmt.setString(2, newEmail)
            stmt.setInt(3, userId)

            val amountOfRowsUpdated = stmt.executeUpdate()

            return@withContext when (amountOfRowsUpdated) {
                0 -> UpdateUserResponse.FailedToFind
                1 -> UpdateUserResponse.Success
                else -> throw SQLUpdateException("More than one user updated")
            }
        }
    }

    suspend fun deleteUser(request: DeleteUserRequest): DeleteUserResponse = withContext(Dispatchers.IO) {
        val userId = request.userId

        this@UserService.dbConn.prepareStatement(DELETE_USER).use { stmt ->
            stmt.setInt(1, userId)

            val amountOfRowsDeleted = stmt.executeUpdate()

            return@withContext when (amountOfRowsDeleted) {
                0 -> DeleteUserResponse.FailedToFind
                1 -> DeleteUserResponse.Success
                else -> throw SQLDeleteException("More than one user deleted")
            }
        }
    }

    suspend fun createPuzzle(request: CreatePuzzleRequest): CreatePuzzleResponse = withContext(Dispatchers.IO) {
        val userId = request.userId
        val json = request.json

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

    suspend fun updatePuzzle(request: UpdatePuzzleRequest): UpdatePuzzleResponse = withContext(Dispatchers.IO) {
        val puzzleId = request.puzzleId
        val json = request.json

        this@UserService.dbConn.prepareStatement(UPDATE_PUZZLE).use { stmt ->
            stmt.setString(1, json)
            stmt.setInt(2, puzzleId)

            val amountOfRowsChanged = stmt.executeUpdate()

            return@withContext when (amountOfRowsChanged) {
                0 -> UpdatePuzzleResponse.FailedToFind
                1 -> UpdatePuzzleResponse.Success
                else -> throw SQLUpdateException("More than one puzzle updated")
            }
        }
    }

    suspend fun deletePuzzle(request: DeletePuzzleRequest): DeletePuzzleResponse = withContext(Dispatchers.IO) {
        val puzzleId = request.puzzleId

        this@UserService.dbConn.prepareStatement(DELETE_PUZZLE).use { stmt ->
            stmt.setInt(1, puzzleId)

            val amountOfRowsDeleted = stmt.executeUpdate()

            return@withContext when (amountOfRowsDeleted) {
                0 -> DeletePuzzleResponse.FailedToFind
                1 -> DeletePuzzleResponse.Success
                else -> throw SQLDeleteException("More than one puzzle deleted")
            }
        }
    }
}
