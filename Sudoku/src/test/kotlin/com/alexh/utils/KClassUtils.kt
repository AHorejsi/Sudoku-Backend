package com.alexh.utils

fun Any.typeName(): String =
    this::class.simpleName!!
