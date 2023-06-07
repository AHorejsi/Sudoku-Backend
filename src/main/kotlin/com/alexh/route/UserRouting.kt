package com.alexh.route

import com.alexh.database.UserService
import java.sql.Connection
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun configureUserService(app: Application, connection: Connection) {
    val service = UserService(connection)

    app.routing {
        this.put("/createUser") {
            createUser(this.call, service)
        }
        this.get("/getUser") {
            getUser(this.call, service)
        }
        this.post("/updateUsername") {
            updateUsername(this.call, service)
        }
        this.post("/updatePassword") {
            updatePassword(this.call, service)
        }
        this.delete("/deleteUser") {
            deleteUser(this.call, service)
        }
    }
}

private suspend fun createUser(call: ApplicationCall, service: UserService) {
    val cookies = call.request.cookies

    val username = cookies["username"]
    val password = cookies["password"]

    if (null === username || null === password) {
        call.respond(HttpStatusCode.BadRequest)
    }
    else {
        service.createUser(username, password)

        call.respond(HttpStatusCode.OK)
    }
}

private suspend fun getUser(call: ApplicationCall, service: UserService) {
    val cookies = call.request.cookies

    val username = cookies["username"]
    val password = cookies["password"]

    if (null === username || null === password) {
        call.respond(HttpStatusCode.BadRequest)
    }
    else {
        val user = service.getUserByLogin(username, password)

        if (null === user) {
            call.respond(HttpStatusCode.Unauthorized)
        }
        else {
            call.respond(HttpStatusCode.OK, user)
        }
    }
}

private suspend fun updateUsername(call: ApplicationCall, service: UserService) {
    val cookies = call.request.cookies

    val userId = cookies["userId"]
    val newUsername = cookies["newUsername"]

    if (null === userId || null === newUsername) {
        call.respond(HttpStatusCode.BadRequest)
    }
    else {
        val intUserId = userId.toInt()

        service.updateUsername(intUserId, newUsername)
    }
}

private suspend fun updatePassword(call: ApplicationCall, service: UserService) {
    val cookies = call.request.cookies

    val userId = cookies["userId"]
    val newPassword = cookies["newPassword"]

    if (null === userId || null === newPassword) {
        call.respond(HttpStatusCode.BadRequest)
    }
    else {
        val intUserId = userId.toInt()

        service.updatePassword(intUserId, newPassword)
    }
}

private suspend fun deleteUser(call: ApplicationCall, service: UserService) {
    val cookies = call.request.cookies

    val userId = cookies["userId"]

    if (null === userId) {
        call.respond(HttpStatusCode.BadRequest)
    }
    else {
        val intUserId = userId.toInt()

        service.deleteUser(intUserId)
    }
}

