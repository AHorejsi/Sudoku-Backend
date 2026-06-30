package com.alexh.utils

import java.text.SimpleDateFormat
import java.util.*

val now: Calendar
    get() = Calendar.getInstance()!!

fun future(timeIntoFutureInMilliseconds: Long): Date {
    if (timeIntoFutureInMilliseconds <= 0) {
        throw IllegalArgumentException("Time must be into the future. So time must be positive")
    }

    return Date(System.currentTimeMillis() + timeIntoFutureInMilliseconds)
}

fun dateString(timeIntoFutureInMilliseconds: Long = 0L): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
    val date = if (0L == timeIntoFutureInMilliseconds) now.time!! else future(timeIntoFutureInMilliseconds)

    return sdf.format(date)
}

fun midnightTomorrow(): Date {
    val date = now

    date.isLenient = true

    date.set(Calendar.HOUR_OF_DAY, 0)
    date.set(Calendar.MINUTE, 0)
    date.set(Calendar.SECOND, 0)

    val day = date.get(Calendar.DAY_OF_MONTH)
    val nextDay = day + 1

    date.set(Calendar.DAY_OF_MONTH, nextDay)

    return date.time!!
}
