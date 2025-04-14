package com.alexh.models

import kotlinx.serialization.Serializable

@Serializable
class GenerateRequest(val dimension: String, val difficulty: String, val games: Set<String>)

@Serializable
class CreateUserRequest(val username: String, val password: String, val email: String)

@Serializable
class ReadUserRequest(val usernameOrEmail: String, val password: String)

@Serializable
class UpdateUserRequest(
    val userId: Int,
    val oldUsername: String,
    val newUsername: String,
    val oldEmail: String,
    val newEmail: String
)

@Serializable
class DeleteUserRequest(val userId: Int)

@Serializable
class CreatePuzzleRequest(val json: String, val userId: Int)

@Serializable
class UpdatePuzzleRequest(val puzzleId: Int, val json: String)

@Serializable
class DeletePuzzleRequest(val puzzleId: Int)
