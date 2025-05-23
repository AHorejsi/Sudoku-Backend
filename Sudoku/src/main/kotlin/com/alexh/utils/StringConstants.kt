package com.alexh.utils

import kotlin.reflect.KClass

class EnvironmentVariables private constructor() {
    init {
        noInstances(EnvironmentVariables::class)
    }

    companion object {
        const val STATIC_SALT = "SUDOKU_SALT"
    }
}

class Endpoints private constructor() {
    init {
        noInstances(Endpoints::class)
    }

    companion object {
        const val GENERATE = "/generate"
        const val CREATE_USER = "/createUser"
        const val UPDATE_USER = "/updateUser"
        const val READ_USER = "/readUser"
        const val DELETE_USER = "/deleteUser"
        const val CREATE_PUZZLE = "/createPuzzle"
        const val UPDATE_PUZZLE = "/updatePuzzle"
        const val DELETE_PUZZLE = "/deletePuzzle"
        const val SHUTDOWN = "/shutdown"
    }
}

class Loggers private constructor() {
    init {
        noInstances(Loggers::class)
    }

    companion object {
        const val MAIN_APPLICATION = "Main"
        const val GENERATION_ROUTING = "Generate-Sudoku-Routing"
        const val USER_ROUTING = "User-Routing"
    }
}

private fun noInstances(cls: KClass<*>): Nothing {
    throw RuntimeException("No instances of ${cls.java.name}")
}
