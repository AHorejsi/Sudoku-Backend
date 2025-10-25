package com.alexh.utils

import java.sql.SQLException

class SQLUpdateException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : SQLException(message, cause)

class SQLDeleteException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : SQLException(message, cause)
