package com.alexh.utils

import java.lang.IllegalArgumentException

fun checkLegal(value: Int?, length: Int) {
    if (null !== value && (value < 1 || value > length)) {
        throw IllegalArgumentException("Illegal Value: $value, Range: 0..$length")
    }
}
