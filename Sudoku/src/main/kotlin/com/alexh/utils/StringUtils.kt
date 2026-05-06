package com.alexh.utils

class EnvironmentVariables private constructor() {
    init {
        noInstances(EnvironmentVariables::class)
    }

    companion object {
        const val STATIC_SALT = "SUDOKU_SALT"

        const val BASIC_REALM = "SUDOKU_BASIC_REALM"
        const val BASIC_NAME = "SUDOKU_BASIC_NAME"
        const val BASIC_PASS = "SUDOKU_BASIC_PASS"

        const val JWT_REALM = "SUDOKU_JWT_REALM"
        const val JWT_SECRET = "SUDOKU_JWT_SECRET"
        const val JWT_ISSUER = "SUDOKU_JWT_ISSUER"
        const val JWT_AUDIENCE = "SUDOKU_JWT_AUDIENCE"

        const val CLIENT_HOST = "SUDOKU_CLIENT_HOST"
        const val CLIENT_PORT = "SUDOKU_CLIENT_PORT"
    }
}

class Endpoints private constructor() {
    init {
        noInstances(Endpoints::class)
    }

    companion object {
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
        const val BASIC = "auth-basic"
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

@Suppress("MemberVisibilityCanBePrivate")
class SqlStrings {
    init {
        noInstances(SqlStrings::class)
    }

    companion object {
        const val DB_NAME = "sudoku"

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

        const val CREATE_DATABASE = "CREATE DATABASE $DB_NAME"

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
            "INSERT INTO $PUZZLE_TABLE ($JSON, $USER_ID)" +
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

fun getEnvironmentVariable(name: String): String {
    return System.getenv(name)!!
}
