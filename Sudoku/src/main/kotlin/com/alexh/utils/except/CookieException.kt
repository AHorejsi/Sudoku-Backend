package com.alexh.utils.except

class CookieException(
    override val message: String = "",
    override val cause: Throwable? = null
) : RuntimeException()