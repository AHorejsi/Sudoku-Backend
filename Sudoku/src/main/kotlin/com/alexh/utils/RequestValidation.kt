package com.alexh.utils

import at.favre.lib.crypto.bcrypt.BCrypt
import kotlin.random.Random

private const val cost = 12
private const val minPasswordLength = 12
private val emailRegex =
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
        "\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+".toRegex()

fun isValidPassword(password: String): Boolean =
    password.length >= minPasswordLength

fun isValidEmail(email: String): Boolean =
    emailRegex.toRegex().matches(email)

fun createPassword(password: String): Pair<String, String> {
    val staticSalt = System.getenv(EnvironmentVariables.STATIC_SALT)
    val dynamicSalt = generateSalt()

    val salted = staticSalt + password + dynamicSalt
    val hashed = BCrypt.withDefaults().hashToString(cost, salted.toCharArray())

    return hashed to dynamicSalt
}

private fun generateSalt(): String {
    val length = 7
    val min = Char.MIN_VALUE.code
    val max = Char.MAX_VALUE.code + 1

    val salt = StringBuilder(length)

    repeat(length) { _ ->
        val char = Random.nextInt(min, max).toChar()

        salt.append(char)
    }

    return salt.toString()
}

fun validatePassword(providedPassword: String, databasePassword: String, dynamicSalt: String): Boolean {
    val staticSalt = System.getenv(EnvironmentVariables.STATIC_SALT)
    val salted = staticSalt + providedPassword + dynamicSalt

    val login = BCrypt.verifyer().verify(salted.toCharArray(), databasePassword)

    return login.verified
}


