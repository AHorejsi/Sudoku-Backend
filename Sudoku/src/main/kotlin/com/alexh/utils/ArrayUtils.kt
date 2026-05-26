package com.alexh.utils

import kotlin.math.pow

inline fun <reified TType> Array<TType>.subsets(): Array<Array<TType>> {
    val subsetCount = 2.0.pow(this.size).toInt()

    val subsets = Array<Array<TType>>(subsetCount) { _ -> emptyArray() }
    var index = 1

    for (subsetSize in 1 .. this.size) {
        for (startIndex in 0 until this.size - subsetSize + 1) {
            subsets[index] = this.sliceArray(startIndex up subsetSize)
            ++index
        }
    }

    return subsets
}
