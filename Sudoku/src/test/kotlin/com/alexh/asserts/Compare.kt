package com.alexh.asserts

import kotlin.test.asserter

fun <TType : Comparable<TType>> assertLess(left: TType, right: TType) {
    assertLess(left, right, naturalOrder())
}

fun <TType> assertLess(left: TType, right: TType, comp: Comparator<TType>) {
    if (comp.compare(left, right) >= 0) {
        asserter.fail("<$left> should be less than <$right>")
    }
}

fun <TType : Comparable<TType>> assertLessOrEqual(left: TType, right: TType) {
    assertLessOrEqual(left, right, naturalOrder())
}

fun <TType> assertLessOrEqual(left: TType, right: TType, comp: Comparator<TType>) {
    if (comp.compare(left, right) > 0) {
        asserter.fail("<$left> should be less than or equal to <$right>")
    }
}

fun <TType : Comparable<TType>> assertGreater(left: TType, right: TType) {
    assertGreater(left, right, naturalOrder())
}

fun <TType> assertGreater(left: TType, right: TType, comp: Comparator<TType>) {
    if (comp.compare(left, right) <= 0) {
        asserter.fail("<$left> should be greater than <$right>")
    }
}

fun <TType : Comparable<TType>> assertGreaterOrEqual(left: TType, right: TType) {
    assertGreaterOrEqual(left, right, naturalOrder())
}

fun <TType> assertGreaterOrEqual(left: TType, right: TType, comp: Comparator<TType>) {
    if (comp.compare(left, right) < 0) {
        asserter.fail("<$left> should be greater than or equal to <$right>")
    }
}
