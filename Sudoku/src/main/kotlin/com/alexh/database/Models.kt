package com.alexh.database

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Int, val username: String, val password: String)

@Serializable
data class Puzzle(val id: Int, val info: String, val reservedBy: Int?)
