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
    @Suppress("UNUSED") val puzzles: List<Puzzle>
)

@Serializable
sealed class LoginAttempt {
    class Success(@Suppress("UNUSED") val user: User) : LoginAttempt()
    object Failure : LoginAttempt()
}
