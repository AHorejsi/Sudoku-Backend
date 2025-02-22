package com.alexh.utils

import kotlin.reflect.KClass

class FormFields private constructor() {
    init {
        noInstances(FormFields::class)
    }

    companion object {
        const val USERNAME = "username"
        const val USERNAME_OR_EMAIL = "usernameOrEmail"
        const val PASSWORD = "password"
        const val EMAIL = "email"
    }
}

class Cookies private constructor() {
    init {
        noInstances(Cookies::class)
    }

    companion object {
        const val DIMENSION = "dimension"
        const val DIFFICULTY = "difficulty"
        const val GAMES = "games"

        const val USER_ID = "userId"

        const val PUZZLE_ID = "puzzleId"
        const val JSON = "json"
    }
}

class Endpoints private constructor() {
    init {
        noInstances(Endpoints::class)
    }

    companion object {
        const val GENERATE = "/generate"

        const val CREATE_USER = "/createUser"
        const val READ_USER = "/readUser"
        const val DELETE_USER = "/deleteUser"

        const val CREATE_PUZZLE = "/createPuzzle"
        const val UPDATE_PUZZLE = "/updatePuzzle"
        const val DELETE_PUZZLE = "/deletePuzzle"
    }
}

private fun <T : Any> noInstances(cls: KClass<T>): Nothing {
    throw Exception("No instances of ${cls.java.name}")
}
