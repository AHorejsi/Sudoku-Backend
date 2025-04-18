package com.alexh.utils

import kotlin.reflect.KClass

class XRequestIds private constructor() {
    init {
        noInstances(XRequestIds::class)
    }

    companion object {
        const val GENERATE = "Generate-Sudoku"
        const val CREATE_USER = "Create-User"
        const val READ_USER = "Read-User"
        const val UPDATE_USER = "Update-User"
        const val DELETE_USER = "Delete-User"
        const val CREATE_PUZZLE = "Create-Puzzle"
        const val UPDATE_PUZZLE = "Update-Puzzle"
        const val DELETE_PUZZLE = "Delete-Puzzle"
    }
}

private fun noInstances(cls: KClass<*>): Nothing {
    throw RuntimeException("No instances of ${cls.java.name}")
}
