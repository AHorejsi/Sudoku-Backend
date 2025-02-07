package com.alexh.models

import kotlinx.serialization.Serializable

@Serializable
class Puzzle(val json: String)

@Serializable
class User(val username: String, val email: String, val puzzles: List<Puzzle>)

@Serializable
sealed class LoginAttempt {
    class Success(val user: User) : LoginAttempt()
    object Failure : LoginAttempt()
}
