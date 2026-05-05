package com.alexh.utils

import at.favre.lib.crypto.bcrypt.BCrypt
import kotlin.random.Random

private const val COST = 12
private const val MIN_PASSWORD_LENGTH = 10
private val PASSWORD_HASHER = BCrypt.withDefaults()!!
private val PASSWORD_VERIFIER = BCrypt.verifyer()!!
private val EMAIL_REGEX =
    ("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
    "\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
    "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+").toRegex()

fun isValidUsername(username: String): Boolean =
    !(username.isEmpty() || isValidEmail(username))

fun isValidPassword(password: String): Boolean =
    password.length >= MIN_PASSWORD_LENGTH

fun isValidEmail(email: String): Boolean =
    EMAIL_REGEX.matches(email)

fun createPassword(password: String): Pair<String, String> {
    val staticSalt = getEnvironmentVariable(EnvironmentVariables.STATIC_SALT)
    val dynamicSalt = generateSalt()

    val salted = (staticSalt + password + dynamicSalt).toCharArray()
    val hashed = PASSWORD_HASHER.hashToString(COST, salted)

    return Pair(hashed, dynamicSalt)
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

fun validatePassword(providedPassword: String, passwordInDatabase: String, dynamicSalt: String): Boolean {
    val staticSalt = getEnvironmentVariable(EnvironmentVariables.STATIC_SALT)
    val salted = (staticSalt + providedPassword + dynamicSalt).toCharArray()

    val login = PASSWORD_VERIFIER.verify(salted, passwordInDatabase)!!

    return login.verified
}


