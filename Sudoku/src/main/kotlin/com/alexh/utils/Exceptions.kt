package com.alexh.utils

import java.sql.SQLException
import kotlin.reflect.KClass

class SqlUpdateException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : SQLException(message, cause)

class SqlDeleteException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : SQLException(message, cause)

fun noInstances(cls: KClass<*>): Nothing {
    throw RuntimeException("No instances of ${cls.java.name}")
}
