package com.alexh.utils

import kotlin.reflect.KClass

fun noInstances(cls: KClass<*>): Nothing {
    throw RuntimeException("No instances of ${cls.java.name}")
}
