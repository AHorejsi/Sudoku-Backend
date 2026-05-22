package com.alexh.utils

import java.sql.SQLException
import kotlin.reflect.KClass

fun noInstances(cls: KClass<*>): Nothing =
    throw RuntimeException("No instances of ${cls.simpleName}")

fun failedDatabaseChange(amount: Int, changeType: String): Nothing =
    throw SQLException("More than one db row changed. Amount: $amount, ChangeType: $changeType")
