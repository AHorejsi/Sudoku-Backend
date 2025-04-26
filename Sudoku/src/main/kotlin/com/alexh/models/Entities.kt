package com.alexh.models

import kotlinx.serialization.Serializable

@Serializable
class Puzzle(
    @Suppress("UNUSED") val id: Int,
    val json: String
)

@Serializable
class User(
    @Suppress("UNUSED") val id: Int,
    val username: String,
    val email: String,
    val puzzles: List<Puzzle>
)
