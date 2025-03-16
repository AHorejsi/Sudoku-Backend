package com.alexh.models

import com.alexh.game.SudokuJson
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
)

@Serializable
sealed class CreateUserResponse {
    @Serializable
    object Success : CreateUserResponse()

    @Serializable
    object DuplicateFound : CreateUserResponse()
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
class DeleteUserRequest(
    val userId: Int,
    val usernameOrEmail: String
)

@Serializable
sealed class DeleteUserResponse {
    @Serializable
    object Success : DeleteUserResponse()

    @Serializable
    object FailedToFind : DeleteUserResponse()
}
