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
    val staticSalt = EnvironmentVariables.STATIC_SALT
    val dynamicSalt = generateSalt()

    val salted = (staticSalt + password + dynamicSalt).toCharArray()
    val hashed = PASSWORD_HASHER.hashToString(COST, salted)

    return Pair(hashed, dynamicSalt)
}

@Suppress("LocalVariableName")
private fun generateSalt(): String {
    val SALT_LENGTH = 7
    val SALT_MIN = Char.MIN_VALUE.code
    val SALT_MAX = Char.MAX_VALUE.code + 1

    val salt = StringBuilder(SALT_LENGTH)

    repeat(SALT_LENGTH) { _ ->
        val char = Random.nextInt(SALT_MIN, SALT_MAX).toChar()

        salt.append(char)
    }

    return salt.toString()
}

fun validatePassword(providedPassword: String, passwordInDatabase: String, dynamicSalt: String): Boolean {
    val staticSalt = EnvironmentVariables.STATIC_SALT
    val salted = (staticSalt + providedPassword + dynamicSalt).toCharArray()

    val login = PASSWORD_VERIFIER.verify(salted, passwordInDatabase)!!

    return login.verified
}


