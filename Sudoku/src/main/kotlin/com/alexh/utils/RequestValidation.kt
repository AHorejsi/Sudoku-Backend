package com.alexh.utils

fun isValidPassword(password: String, minLength: Int): Boolean =
    password.length >= minLength

fun isValidEmail(email: String): Boolean {
    val pattern = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"

    return pattern.toRegex().matches(email)
}

fun createPassword(password: String, salt1: String, salt2: String): String =
    salt1 + password + salt2