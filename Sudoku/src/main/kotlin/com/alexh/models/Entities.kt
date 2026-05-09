package com.alexh.models

import kotlinx.serialization.Serializable

@Serializable
class Puzzle(
    @Suppress("UNUSED") val id: Int,
    @Suppress("UNUSED") val json: String
)

@Serializable
class User(
    val id: Int,
    val username: String,
    val email: String,
    val puzzles: List<Puzzle>
)
