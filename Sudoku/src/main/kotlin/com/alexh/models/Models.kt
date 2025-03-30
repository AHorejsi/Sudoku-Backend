package com.alexh.models

import com.alexh.game.SudokuJson
import kotlinx.serialization.Serializable

@Serializable
class GenerateRequest(
    val dimension: String,
    val difficulty: String,
    val games: Set<String>
)

@Serializable
sealed class GenerateResponse {
    @Serializable
    class Success(@Suppress("UNUSED") val puzzle: SudokuJson) : GenerateResponse()

    @Serializable
    object UnfilledFields : GenerateResponse()
}

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

    @Serializable
    object FailedToCreate : CreateUserResponse()

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

@Serializable
class CreatePuzzleRequest(val json: String, val userId: Int)

@Serializable
sealed class CreatePuzzleResponse {
    @Serializable
    class Success(@Suppress("UNUSED") val puzzle: Puzzle) : CreatePuzzleResponse()

    @Serializable
    object FailedToCreate : CreatePuzzleResponse()
}

@Serializable
class UpdatePuzzleRequest(val puzzleId: Int, val json: String)

@Serializable
sealed class UpdatePuzzleResponse {
    @Serializable
    object Success : UpdatePuzzleResponse()

    @Serializable
    object FailedToFind : UpdatePuzzleResponse()
}

@Serializable
class DeletePuzzleRequest(val puzzleId: Int)

@Serializable
sealed class DeletePuzzleResponse {
    @Serializable
    object Success : DeletePuzzleResponse()

    @Serializable
    object FailedToFind : DeletePuzzleResponse()
}
