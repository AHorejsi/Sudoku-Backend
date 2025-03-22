package com.alexh.models

import com.alexh.game.SudokuJson
import com.alexh.utils.isValidEmail
import com.alexh.utils.isValidPassword
import kotlinx.serialization.Serializable

@Serializable
class Puzzle(
    @Suppress("UNUSED") val id: Int,
    @Suppress("UNUSED") val json: String
)

@Serializable
class User(
    @Suppress("UNUSED") val id: Int,
    @Suppress("UNUSED") val username: String,
    @Suppress("UNUSED") val email: String,
    @Suppress("UNUSED") val puzzles: List<Puzzle>
)

@Serializable
class GenerateRequest(
    val dimension: String,
    val difficulty: String,
    val games: Set<String>
)

@Serializable
class GenerateResponse(@Suppress("UNUSED") val puzzle: SudokuJson)

@Serializable
class CreateUserRequest(
    val username: String,
    val password: String,
    val email: String
) {
    companion object {
        val MIN_PASSWORD_LENGTH = 12
    }

    val valid: Boolean
        get() = isValidPassword(this.password, CreateUserRequest.MIN_PASSWORD_LENGTH) && isValidEmail(this.email)
}

@Serializable
sealed class CreateUserResponse {
    @Serializable
    object Success : CreateUserResponse()

    @Serializable
    object DuplicateFound : CreateUserResponse()

    @Serializable
    object ConditionsFailed : CreateUserResponse()
}

@Serializable
class ReadUserRequest(
    val usernameOrEmail: String,
    val password: String
)

@Serializable
sealed class ReadUserResponse {
    @Serializable
    class Success(@Suppress("UNUSED") val user: User) : ReadUserResponse()

    @Serializable
    object FailedToFind : ReadUserResponse()
}

@Serializable
class UpdateUserRequest(
    val userId: Int,
    val oldUsername: String,
    val newUsername: String,
    val oldEmail: String,
    val newEmail: String
)

@Serializable
sealed class UpdateUserResponse {
    @Serializable
    class Success(
        @Suppress("UNUSED") val newUsername: String,
        @Suppress("UNUSED") val newEmail: String
    ) : UpdateUserResponse()

    @Serializable
    object FailedToFind : UpdateUserResponse()
}

@Serializable
class DeleteUserRequest(val userId: Int)

@Serializable
sealed class DeleteUserResponse {
    @Serializable
    object Success : DeleteUserResponse()

    @Serializable
    object FailedToFind : DeleteUserResponse()
}
