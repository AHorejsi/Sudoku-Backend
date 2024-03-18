package com.alexh.utils

import kotlin.reflect.KClass

class Cookies private constructor() {
    init {
        noInstanceError(Cookies::class)
    }

    companion object {
        const val DIMENSION = "dimension"
        const val DIFFICULTY = "difficulty"
        const val GAMES = "games"
    }
}

class Endpoints private constructor() {
    init {
        noInstanceError(Endpoints::class)
    }

    companion object {
        const val GENERATION = "/generate"
        const val SOLVED = "/solved"
    }
}

private fun <T : Any> noInstanceError(cls: KClass<T>): Nothing {
    throw InternalError("No instances of ${cls.java.name}")
}
