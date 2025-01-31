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

        const val NAME = "name"
        const val EMAIL = "email"
        const val NAME_OR_EMAIL = "nameOrEmail"
        const val PASSWORD = "password"
    }
}

class Endpoints private constructor() {
    init {
        noInstances(Endpoints::class)
    }

    companion object {
        const val GENERATION = "/generate"

        const val CREATE_USER_LOGIN = "/createUser"
        const val GET_USER_BY_LOGIN = "/getUser"
        const val DELETE_USER_BY_ID = "/deleteUser"
    }
}

private fun <T : Any> noInstances(cls: KClass<T>): Nothing {
    throw Exception("No instances of ${cls.java.name}")
}
