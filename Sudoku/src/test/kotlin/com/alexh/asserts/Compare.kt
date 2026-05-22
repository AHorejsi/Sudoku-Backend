package com.alexh.asserts

import kotlin.test.asserter

fun <TType : Comparable<TType>> assertLess(left: TType, right: TType) =
    assertLess(left, right) { lhs, rhs -> lhs.compareTo(rhs) }

fun <TType> assertLess(left: TType, right: TType, comp: (TType, TType) -> Int) {
    if (comp(left, right) >= 0) {
        asserter.fail("<$left> should be less than <$right>")
    }
}
