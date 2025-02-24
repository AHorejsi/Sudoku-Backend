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
sealed class LoginAttempt {
    @Serializable
    class Success(@Suppress("UNUSED") val user: User) : LoginAttempt()

    @Serializable
    object Failure : LoginAttempt()
}

@Serializable
sealed class UserCreationAttempt {
    @Serializable
    object Success : UserCreationAttempt()

    @Serializable
    object DuplicateAdded : UserCreationAttempt()
}
