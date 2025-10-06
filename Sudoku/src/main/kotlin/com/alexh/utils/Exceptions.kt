package com.alexh.utils

class JwtException(
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)
