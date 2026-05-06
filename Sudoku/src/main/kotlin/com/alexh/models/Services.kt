package com.alexh.models

import com.alexh.route.createJwtToken
import com.alexh.route.refreshJwtTokenIfExpired
import com.alexh.utils.*
import io.ktor.server.auth.jwt.*
import java.sql.*

fun createUser(dbConn: Connection, request: CreateUserRequest): CreateUserResponse {
    val username = request.username.trim()
    val password = request.password
    val email = request.email.trim()

    val valid = checkCreateForValidity(username, password, email)

    if (null !== valid) {
        return valid
    }

    dbConn.prepareStatement(SqlStrings.CREATE_USER, Statement.RETURN_GENERATED_KEYS).use { stmt ->
        val (passwordHash, salt) = createPassword(password)

        stmt.setString(1, username)
        stmt.setString(2, passwordHash)
        stmt.setString(3, email)
        stmt.setString(4, salt)

        return doUserCreation(stmt)
    }
}

private fun checkCreateForValidity(username: String, password: String, email: String): CreateUserResponse? =
    if (!isValidUsername(username))
        CreateUserResponse.InvalidUsername
    else if (!isValidPassword(password))
        CreateUserResponse.InvalidPassword
    else if (!isValidEmail(email))
        CreateUserResponse.InvalidEmail
    else
        null

fun doUserCreation(stmt: PreparedStatement): CreateUserResponse {
    runCatching {
        stmt.executeUpdate()
    }.onFailure { ex ->
        val message = ex.message

        if (null !== message && message.startsWith("Unique")) {
            return CreateUserResponse.DuplicateFound
        }

        throw ex
    }

    stmt.generatedKeys.use { keys ->
        if (!keys.next()) {
            return CreateUserResponse.FailedToCreate
        }
    }

    return CreateUserResponse.Success
}

fun readUserWithPassword(dbConn: Connection, request: ReadUserRequest): ReadUserResponse {
    val usernameOrEmail = request.usernameOrEmail.trim()
    val password = request.password

    dbConn.prepareStatement(SqlStrings.GET_USER).use { stmt ->
        stmt.setString(1, usernameOrEmail)
        stmt.setString(2, usernameOrEmail)

        stmt.executeQuery().use { results ->
            val user = buildUserWithPassword(results, password)

            if (null === user) {
                return ReadUserResponse.FailedToFind
            }
            else {
                val token = createJwtToken(usernameOrEmail)

                return ReadUserResponse.Success(user, token)
            }
        }
    }
}

private fun buildUserWithPassword(results: ResultSet, providedPassword: String): User? {
    val user = buildUserObject(results)

    if (null !== user) {
        val passwordInDatabase = results.getString(SqlStrings.PASSWORD)
        val dynamicSalt = results.getString(SqlStrings.SALT)

        if (!validatePassword(providedPassword, passwordInDatabase, dynamicSalt)) {
            return null
        }
    }

    return user
}

fun readUserWithToken(dbConn: Connection, principal: JWTPrincipal): TokenLoginResponse {
    val usernameOrEmail = principal.payload.claims.getValue(JwtClaims.USERNAME_OR_EMAIL).asString()

    dbConn.prepareStatement(SqlStrings.GET_USER).use { stmt ->
        stmt.setString(1, usernameOrEmail)
        stmt.setString(2, usernameOrEmail)

        stmt.executeQuery().use { results ->
            val user = buildUserObject(results)

            if (null === user) {
                return TokenLoginResponse.InvalidUsernameOrEmail
            }

            val newToken = refreshJwtTokenIfExpired(user, principal.payload)

            return if (null !== newToken)
                TokenLoginResponse.Success(user, newToken)
            else
                TokenLoginResponse.Expired
        }
    }
}

private fun buildUserObject(results: ResultSet): User? {
    if (!results.next()) {
        return null
    }

    val userId = results.getInt(SqlStrings.USER_ID)
    val username = results.getString(SqlStrings.USERNAME)
    val email = results.getString(SqlStrings.EMAIL)

    val puzzleIds = results.getString(SqlStrings.PUZZLE_ID)?.split(SqlStrings.SEPARATOR)
    val puzzleJsons = results.getString(SqlStrings.JSON)?.split(SqlStrings.SEPARATOR)
    val puzzles = makePuzzleList(puzzleIds, puzzleJsons)

    return User(userId, username, email, puzzles)
}

private fun makePuzzleList(idList: List<String>?, jsonList: List<String>?): List<Puzzle> {
    val puzzleList = mutableListOf<Puzzle>()

    if (null !== idList && null !== jsonList) {
        for (index in idList.indices) {
            val id = idList[index].toInt()
            val json = jsonList[index]

            val puzzle = Puzzle(id, json)

            puzzleList.add(puzzle)
        }
    }

    return puzzleList
}

fun updateUser(dbConn: Connection, request: UpdateUserRequest): UpdateUserResponse {
    val userId = request.userId
    val newUsername = request.newUsername.trim()
    val newEmail = request.newEmail.trim()

    val valid = checkUpdateForValidity(newUsername, newEmail)

    if (null !== valid) {
        return valid
    }

    dbConn.prepareStatement(SqlStrings.UPDATE_USER).use { stmt ->
        stmt.setString(1, newUsername)
        stmt.setString(2, newEmail)
        stmt.setInt(3, userId)

        val amountOfRowsUpdated = stmt.executeUpdate()

        return when (amountOfRowsUpdated) {
            0 -> UpdateUserResponse.FailedToFind
            1 -> UpdateUserResponse.Success
            else -> throw SQLException("More than one user updated")
        }
    }
}

private fun checkUpdateForValidity(username: String, email: String): UpdateUserResponse? =
    if (!isValidUsername(username))
        UpdateUserResponse.InvalidUsername
    else if (!isValidEmail(email))
        UpdateUserResponse.InvalidEmail
    else
        null

fun deleteUser(dbConn: Connection, request: DeleteUserRequest): DeleteUserResponse {
    val userId = request.userId

    dbConn.prepareStatement(SqlStrings.DELETE_USER).use { stmt ->
        stmt.setInt(1, userId)

        val amountOfRowsDeleted = stmt.executeUpdate()

        return when (amountOfRowsDeleted) {
            0 -> DeleteUserResponse.FailedToFind
            1 -> DeleteUserResponse.Success
            else -> throw SQLException("More than one user deleted")
        }
    }
}

fun createPuzzle(dbConn: Connection, request: CreatePuzzleRequest): CreatePuzzleResponse {
    val userId = request.userId
    val json = request.json

    dbConn.prepareStatement(SqlStrings.CREATE_PUZZLE, Statement.RETURN_GENERATED_KEYS).use { stmt ->
        stmt.setString(1, json)
        stmt.setInt(2, userId)

        return doPuzzleCreation(stmt, json)
    }
}

private fun doPuzzleCreation(stmt: PreparedStatement, json: String): CreatePuzzleResponse {
    stmt.executeUpdate()

    stmt.generatedKeys.use { keys ->
        if (!keys.next()) {
            return CreatePuzzleResponse.FailedToCreate
        }

        val id = keys.getInt(SqlStrings.PUZZLE_TABLE_ID)

        val puzzle = Puzzle(id, json)

        return CreatePuzzleResponse.Success(puzzle)
    }
}

fun updatePuzzle(dbConn: Connection, request: UpdatePuzzleRequest): UpdatePuzzleResponse {
    val puzzleId = request.puzzleId
    val json = request.json

    dbConn.prepareStatement(SqlStrings.UPDATE_PUZZLE).use { stmt ->
        stmt.setString(1, json)
        stmt.setInt(2, puzzleId)

        val amountOfRowsChanged = stmt.executeUpdate()

        return when (amountOfRowsChanged) {
            0 -> UpdatePuzzleResponse.FailedToFind
            1 -> UpdatePuzzleResponse.Success
            else -> throw SQLException("More than one puzzle updated")
        }
    }
}

fun deletePuzzle(dbConn: Connection, request: DeletePuzzleRequest): DeletePuzzleResponse {
    val puzzleId = request.puzzleId

    dbConn.prepareStatement(SqlStrings.DELETE_PUZZLE).use { stmt ->
        stmt.setInt(1, puzzleId)

        val amountOfRowsDeleted = stmt.executeUpdate()

        return when (amountOfRowsDeleted) {
            0 -> DeletePuzzleResponse.FailedToFind
            1 -> DeletePuzzleResponse.Success
            else -> throw SQLException("More than one puzzle deleted")
        }
    }
}
