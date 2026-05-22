package com.alexh.utils

import java.text.SimpleDateFormat
import java.util.*

fun future(futureTimeInMillis: Long): Date {
    require(futureTimeInMillis > 0) { "Time must be into the future. So time must be positive" }

    return Date(System.currentTimeMillis() + futureTimeInMillis)
}

fun dateString(futureTimeInMillis: Long = 0L): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
    val date = if (0L == futureTimeInMillis) Date() else future(futureTimeInMillis)

    return sdf.format(date)
}
