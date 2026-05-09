package com.alexh.utils

import io.ktor.server.plugins.ratelimit.*

class EnvironmentVariables private constructor() {
    init {
        noInstances(EnvironmentVariables::class)
    }

    companion object {
        val STATIC_SALT = System.getenv("STATIC_SALT")!!

        val JWT_REALM = System.getenv("JWT_REALM")!!
        val JWT_SECRET = System.getenv("JWT_SECRET")!!
        val JWT_ISSUER = System.getenv("JWT_ISSUER")!!
        val JWT_AUDIENCE = System.getenv("JWT_AUDIENCE")!!

        val CLIENT_HOST = System.getenv("CLIENT_HOST")!!
        val CLIENT_PORT = System.getenv("CLIENT_PORT")!!
    }
}

class Endpoints private constructor() {
    init {
        noInstances(Endpoints::class)
    }

    companion object {
        const val PING = "/ping"
        const val GENERATE = "/generate"
        const val CREATE_USER = "/createUser"
        const val UPDATE_USER = "/updateUser"
        const val READ_USER = "/readUser"
        const val DELETE_USER = "/deleteUser"
        const val CREATE_PUZZLE = "/createPuzzle"
        const val UPDATE_PUZZLE = "/updatePuzzle"
        const val DELETE_PUZZLE = "/deletePuzzle"
        const val TOKEN_LOGIN = "/tokenLogin"
        const val RENEW_TOKEN = "/renewToken"
        const val SHUTDOWN = "/shutdown"
    }
}

class Loggers private constructor() {
    init {
        noInstances(Loggers::class)
    }

    companion object {
        const val MAIN_APPLICATION = "Main"
        const val GENERATION_ROUTING = "Generate-Sudoku-Routing"
        const val USER_ROUTING = "User-Routing"
    }
}

class Auths private constructor() {
    init {
        noInstances(Auths::class)
    }

    companion object {
        const val DIGEST = "auth-digest"
        const val JWT = "auth-jwt"
    }
}

class JwtClaims private constructor() {
    init {
        noInstances(JwtClaims::class)
    }

    companion object {
        const val USERNAME_OR_EMAIL = "usernameOrEmail"
    }
}

class RateLimits private constructor() {
    init {
        noInstances(RateLimits::class)
    }

    companion object {
        const val SUDOKU_GENERATE_LIMIT = 1000
        const val SUDOKU_GENERATE_REFILL_PERIOD = 60 // one minute in seconds
        val SUDOKU_GENERATE_NAME = RateLimitName("Sudoku Generation Endpoint Set")

        const val USER_ACCOUNT_INTERACTION_LIMIT = 100
        const val USER_ACCOUNT_INTERACTION_REFILL_PERIOD = 3600 // one hour in seconds
        val USER_ACCOUNT_INTERACTION_NAME = RateLimitName("User Account Interaction Endpoint Set")

        const val LIMIT_HEADER = "X-RateLimit-Limit"
        const val REMAINING_HEADER = "X-RateLimit-Remaining"
        const val RESET_HEADER = "X-RateLimit-Reset"
        const val RETRY_AFTER_HEADER = "Retry-After"
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class SqlStrings private constructor() {
    init {
        noInstances(SqlStrings::class)
    }

    companion object {
        const val DB_DRIVER = "org.postgresql.Driver"

        const val USER_TABLE = "Users"
        const val USER_TABLE_ID = "id"
        const val USERNAME = "username"
        const val PASSWORD = "password"
        const val EMAIL = "email"
        const val SALT = "salt"

        const val PUZZLE_TABLE = "Puzzles"
        const val PUZZLE_TABLE_ID = "id"
        const val JSON = "json"

        const val USER_ID = "userId"
        const val PUZZLE_ID = "puzzleId"

        const val SEPARATOR = "|"

        const val CREATE_USER_TABLE =
            "CREATE TABLE IF NOT EXISTS $USER_TABLE (" +
                "$USER_TABLE_ID SERIAL PRIMARY KEY," +
                "$USERNAME TEXT UNIQUE NOT NULL," +
                "$PASSWORD TEXT NOT NULL," +
                "$EMAIL TEXT UNIQUE NOT NULL," +
                "$SALT TEXT NOT NULL" +
            ")"
        const val CREATE_PUZZLE_TABLE =
            "CREATE TABLE IF NOT EXISTS $PUZZLE_TABLE (" +
                "$PUZZLE_TABLE_ID SERIAL PRIMARY KEY," +
                "$JSON TEXT NOT NULL," +
                "$USER_ID INT REFERENCES $USER_TABLE($USER_TABLE_ID) " +
                "ON UPDATE CASCADE " +
                "ON DELETE CASCADE" +
            ")"

        const val CREATE_USER =
            "INSERT INTO $USER_TABLE ($USERNAME, $PASSWORD, $EMAIL, $SALT) " +
            "VALUES (?, ?, ?, ?)"
        const val GET_USER =
            "SELECT $USER_TABLE.$USER_TABLE_ID AS $USER_ID, $USERNAME, $EMAIL, $PASSWORD, $SALT," +
            "STRING_AGG(CAST($PUZZLE_TABLE.$PUZZLE_TABLE_ID AS TEXT), '$SEPARATOR') AS $PUZZLE_ID," +
            "STRING_AGG($JSON, '$SEPARATOR') as $JSON " +
            "FROM $USER_TABLE " +
            "LEFT JOIN $PUZZLE_TABLE ON $USER_TABLE.$USER_TABLE_ID = $PUZZLE_TABLE.$USER_ID " +
            "WHERE LOWER($USERNAME) = LOWER(?) OR LOWER($EMAIL) = LOWER(?)" +
            "GROUP BY $USER_TABLE.$USER_TABLE_ID"
        const val UPDATE_USER =
            "UPDATE $USER_TABLE " +
            "SET $USERNAME = ?, $EMAIL = ? " +
            "WHERE $USER_TABLE_ID = ?"
        const val DELETE_USER =
            "DELETE FROM $USER_TABLE " +
            "WHERE $USER_TABLE_ID = ?"

        const val CREATE_PUZZLE =
            "INSERT INTO $PUZZLE_TABLE ($JSON, $USER_ID) " +
            "VALUES (?, ?)"
        const val UPDATE_PUZZLE =
            "UPDATE $PUZZLE_TABLE " +
            "SET $JSON = ? " +
            "WHERE $PUZZLE_TABLE_ID = ?"
        const val DELETE_PUZZLE =
            "DELETE FROM $PUZZLE_TABLE " +
            "WHERE $PUZZLE_TABLE_ID = ?"
    }
}
