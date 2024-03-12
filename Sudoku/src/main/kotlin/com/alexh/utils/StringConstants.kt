package com.alexh.utils

class Cookies private constructor() {
    init {
        throw InternalError("No instances of ${Cookies.Companion::class.java.name}")
    }

    companion object {
        const val DIMENSION = "dimension"
        const val DIFFICULTY = "difficulty"
        const val GAMES = "games"

    }
}

class Endpoints private constructor() {
    init {
        throw InternalError("No instances of ${Endpoints.Companion::class.java.name}")
    }

    companion object {
        const val GENERATION = "/generate"
    }
}