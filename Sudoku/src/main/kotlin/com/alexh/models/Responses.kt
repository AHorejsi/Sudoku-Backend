package com.alexh.models

import com.alexh.game.SudokuJson
import kotlinx.serialization.Serializable

@Serializable
sealed class GenerateResponse {
    @Serializable
    class Success(@Suppress("UNUSED") val puzzle: SudokuJson) : GenerateResponse()

    @Serializable
    object UnfilledFields : GenerateResponse()
}

@Serializable
sealed class CreateUserResponse {
    @Serializable
    object Success : CreateUserResponse()

    @Serializable
    object DuplicateFound : CreateUserResponse()

    @Serializable
    object InvalidUsername : CreateUserResponse()

    @Serializable
    object InvalidPassword : CreateUserResponse()

    @Serializable
    object InvalidEmail : CreateUserResponse()

    @Serializable
    object FailedToCreate : CreateUserResponse()
}

@Serializable
sealed class ReadUserResponse {
    @Serializable
    class Success(@Suppress("UNUSED") val user: User) : ReadUserResponse()

    @Serializable
    object FailedToFind : ReadUserResponse()
}

@Serializable
sealed class UpdateUserResponse {
    @Serializable
    object Success : UpdateUserResponse()

    @Serializable
    object InvalidUsername : UpdateUserResponse()

    @Serializable
    object InvalidEmail : UpdateUserResponse()

    @Serializable
    object FailedToFind : UpdateUserResponse()
}

@Serializable
sealed class DeleteUserResponse {
    @Serializable
    object Success : DeleteUserResponse()

    @Serializable
    object FailedToFind : DeleteUserResponse()
}

@Serializable
sealed class CreatePuzzleResponse {
    @Serializable
    class Success(@Suppress("UNUSED") val puzzle: Puzzle) : CreatePuzzleResponse()

    @Serializable
    object FailedToCreate : CreatePuzzleResponse()
}

@Serializable
sealed class UpdatePuzzleResponse {
    @Serializable
    object Success : UpdatePuzzleResponse()

    @Serializable
    object FailedToFind : UpdatePuzzleResponse()
}

@Serializable
sealed class DeletePuzzleResponse {
    @Serializable
    object Success : DeletePuzzleResponse()

    @Serializable
    object FailedToFind : DeletePuzzleResponse()
}
