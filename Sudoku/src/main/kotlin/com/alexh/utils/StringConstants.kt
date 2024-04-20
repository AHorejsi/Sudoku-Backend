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
    }
}

class Endpoints private constructor() {
    init {
        noInstances(Endpoints::class)
    }

    companion object {
        const val GENERATION = "/generate"
    }
}

private fun <T : Any> noInstances(cls: KClass<T>): Nothing {
    throw Exception("No instances of ${cls.java.name}")
}
