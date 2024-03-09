package com.alexh.utils

fun Collection<*>.combineHashCodes(): Int {
    var hashValue = 0

    for (value in this) {
        val newHash = value.hashCode()

        hashValue = hashValue.combineHashCode(newHash)
    }

    return hashValue
}

fun Int.combineHashCode(value: Int): Int = this + value * 31