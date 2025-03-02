package com.alexh.models

import kotlinx.serialization.Serializable

@Serializable
class Puzzle(
    val id: Int,
    val json: String
)

@Serializable
class User(
    val id: Int,
    val username: String,
    val email: String,
    val puzzles: List<Puzzle>
)

@Serializable
class GenerateRequest(
    val dimension: String,
    val difficulty: String,
    val games: Set<String>
)

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
    val newUsername: String,
    val newEmail: String,
    val password: String
)

@Serializable
sealed class UpdateUserResponse {
    @Serializable
    class Success(
        @Suppress("UNUSED")
        val newUsername: String,
        @Suppress("UNUSED")
        val newEmail: String
    ) : UpdateUserResponse()

    @Serializable
    object FailedToFind : UpdateUserResponse()
}

@Serializable
class DeleteUserRequest(
    val usernameOrEmail: String,
    val password: String
)

@Serializable
sealed class DeleteUserResponse {
    @Serializable
    object Success : DeleteUserResponse()

    @Serializable
    object FailedToFind : DeleteUserResponse()
}
