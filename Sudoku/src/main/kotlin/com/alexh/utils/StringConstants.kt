package com.alexh.utils

import kotlin.reflect.KClass

class Cookies private constructor() {
    init {
        noInstances(Cookies::class)
    }

    companion object {
        const val DIMENSION = "dimension"
        const val DIFFICULTY = "difficulty"
        const val GAMES = "games"

        const val JSON = "json"
    }
}

class Endpoints private constructor() {
    init {
        noInstances(Endpoints::class)
    }

    companion object {
        const val GENERATION = "/generate"

        const val CREATE_USER = "/createUser"
        const val GET_USER = "/getUser"
        const val DELETE_USER = "/deleteUser"

        const val CREATE_PUZZLE = "/createPuzzle"
        const val UPDATE_PUZZLE = "/updatePuzzle"
        const val DELETE_PUZZLE = "/deletePuzzle"
    }
}

private fun <T : Any> noInstances(cls: KClass<T>): Nothing {
    throw Exception("No instances of ${cls.java.name}")
}
