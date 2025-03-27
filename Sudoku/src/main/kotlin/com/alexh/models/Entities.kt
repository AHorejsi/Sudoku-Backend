package com.alexh.models

import kotlinx.serialization.Serializable

@Serializable
class Puzzle(
    @Suppress("UNUSED") val id: Int,
    @Suppress("UNUSED") val json: String
)

@Serializable
class User(
    @Suppress("UNUSED") val id: Int,
    @Suppress("UNUSED") val username: String,
    @Suppress("UNUSED") val email: String,
    @Suppress("UNUSED") val puzzles: List<Puzzle>
)
