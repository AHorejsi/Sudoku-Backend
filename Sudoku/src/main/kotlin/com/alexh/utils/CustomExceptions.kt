package com.alexh.utils

class CustomExceptions(
    override val message: String = "",
    override val cause: Throwable? = null
) : RuntimeException()
