package com.alexh.models

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection

@Serializable
data class User(val id: Int, val name: String, val email: String)

class UserService(private val conn: Connection) {
    companion object {
        const val TABLE_NAME = "Users"
        const val ID_FIELD = "id"
        const val NAME_FIELD = "name"
        const val PASSWORD_FIELD = "password"
        const val EMAIL_FIELD = "email"

        private const val CREATE_USER = "INSERT INTO $TABLE_NAME ($NAME_FIELD, $EMAIL_FIELD, $PASSWORD_FIELD) VALUES (?, ?, ?)"
        private const val GET_USER_BY_LOGIN = "SELECT * FROM $TABLE_NAME WHERE ($NAME_FIELD = ? OR $EMAIL_FIELD = ?) AND $PASSWORD_FIELD = ?"
        private const val DELETE_USER_BY_ID = "DELETE FROM $TABLE_NAME WHERE $ID_FIELD = ?"
    }

    suspend fun create(name: String, email: String, password: String): Int? = withContext(Dispatchers.IO) {
        val stmt = this@UserService.conn.prepareStatement(CREATE_USER)

        stmt.setString(1, name)
        stmt.setString(2, email)
        stmt.setString(3, password)

        stmt.executeUpdate()

        val generatedKeys = stmt.generatedKeys

        if (!generatedKeys.next()) {
            throw Exception("Unable to retrieve the id of the newly inserted user")
        }
        else {
            return@withContext generatedKeys.getInt(ID_FIELD)
        }
    }

    suspend fun read(nameOrEmail: String, password: String): User? = withContext(Dispatchers.IO) {
        val stmt = this@UserService.conn.prepareStatement(GET_USER_BY_LOGIN)

        stmt.setString(1, nameOrEmail)
        stmt.setString(2, nameOrEmail)
        stmt.setString(3, password)

        val results = stmt.executeQuery()

        if (!results.next()) {
            return@withContext null
        }
        else {
            val id = results.getInt(ID_FIELD)
            val name = results.getString(NAME_FIELD)
            val email = results.getString(EMAIL_FIELD)

            return@withContext User(id, name, email)
        }
    }

    suspend fun delete(user: User): Unit = withContext(Dispatchers.IO) {
        val stmt = this@UserService.conn.prepareStatement(DELETE_USER_BY_ID)

        stmt.setInt(1, user.id)

        stmt.executeUpdate()
    }
}