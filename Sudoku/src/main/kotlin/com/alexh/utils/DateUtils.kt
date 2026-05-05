package com.alexh.utils

import java.text.SimpleDateFormat
import java.util.*

fun dateString(date: Date): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")

    return sdf.format(date)
}

fun currentDateString(): String =
    dateString(Date())

fun oneWeekFromNow(): Date {
    val oneWeekFromNow = System.currentTimeMillis() + 604800000

    return Date(oneWeekFromNow)
}