package com.alexh.utils

import kotlin.reflect.KClass

class EnvironmentVariables private constructor() {
    init {
        noInstances(EnvironmentVariables::class)
    }

    companion object {
        const val STATIC_SALT = "SUDOKU_SALT"
        const val JWT_SECRET = "SUDOKU_JWT_SECRET"
        const val JWT_ISSUER = "SUDOKU_JWT_ISSUER"
        const val JWT_AUDIENCE = "SUDOKU_JWT_AUDIENCE"
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

class Auths private constructor() {
    init {
        noInstances(Auths::class)
    }

    companion object {
        const val JWT = "auth-jwt"
    }
}

class JwtClaims private constructor() {
    init {
        noInstances(JwtClaims::class)
    }

    companion object {
        const val USERNAME_OR_EMAIL = "usernameOrEmail"
    }
}

private fun noInstances(cls: KClass<*>): Nothing {
    throw RuntimeException("No instances of ${cls.java.name}")
}
