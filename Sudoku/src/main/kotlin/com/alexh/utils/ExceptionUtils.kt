package com.alexh.utils

import java.sql.SQLException
import kotlin.reflect.KClass

internal fun noInstances(cls: KClass<*>): Nothing =
    throw RuntimeException("No instances of ${cls.simpleName}")

internal fun failedDatabaseChange(amount: Int, changeType: String): Nothing =
    throw SQLException("More than one db row changed. Amount: $amount, ChangeType: $changeType")
