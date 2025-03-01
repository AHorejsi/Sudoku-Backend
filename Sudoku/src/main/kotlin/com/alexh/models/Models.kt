package com.alexh.models

import kotlinx.serialization.Serializable

@Serializable
data class Puzzle(
    val id: Int,
    val json: String
)

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val puzzles: List<Puzzle>
)

@Serializable
sealed class CreateUserAttempt {
    @Serializable
    object Success : CreateUserAttempt()

    @Serializable
    object DuplicateFound : CreateUserAttempt()
}

@Serializable
sealed class ReadUserAttempt {
    @Serializable
    class Success(@Suppress("UNUSED") val user: User) : ReadUserAttempt()

    @Serializable
    object FailedToFind : ReadUserAttempt()
}

@Serializable
sealed class UpdateUserAttempt {
    @Serializable
    class Success(
        @Suppress("UNUSED")
        val newUsername: String,
        @Suppress("UNUSED")
        val newEmail: String
    ) : UpdateUserAttempt()

    @Serializable
    object FailedToFind : UpdateUserAttempt()
}

@Serializable
sealed class DeleteUserAttempt {
    @Serializable
    object Success : DeleteUserAttempt()

    @Serializable
    object FailedToFind : DeleteUserAttempt()
}
