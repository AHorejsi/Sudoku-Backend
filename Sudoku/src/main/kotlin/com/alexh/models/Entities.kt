package com.alexh.models

import kotlinx.serialization.Serializable

@Suppress("UNUSED")
@Serializable
class Puzzle(
    val id: Int,
    val json: String
)

@Serializable
class User(
    val id: Int,
    val username: String,
    val email: String,
    val puzzles: List<Puzzle>
)
