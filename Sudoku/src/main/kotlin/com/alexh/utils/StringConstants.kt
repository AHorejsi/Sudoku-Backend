package com.alexh.utils

import kotlin.reflect.KClass

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

private fun <T : Any> noInstances(cls: KClass<T>): Nothing {
    throw RuntimeException("No instances of ${cls.java.name}")
}
