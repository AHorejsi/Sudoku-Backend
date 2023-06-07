package com.alexh.database

import kotlinx.serialization.Serializable

@Serializable
class User(val id: Int, val username: String, val password: String)

@Serializable
class Puzzle(val id: Int, val info: String, val reservedBy: Int)
