package com.alexh.utils

class CookieException(
    override val message: String = "",
    override val cause: Throwable? = null
) : RuntimeException()

class JwtException(
    override val message: String = "",
    override val cause: Throwable? = null
) : RuntimeException()
