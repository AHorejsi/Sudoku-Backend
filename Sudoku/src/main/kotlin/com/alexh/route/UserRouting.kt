package com.alexh.route

import com.alexh.database.UserService
import com.alexh.plugins.connect
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun configureUserService(app: Application) {
    app.routing {
        this.put("/createUser") {
            createUser(this.call, app)
        }
        this.get("/getUser") {
            getUser(this.call, app)
        }
    }
}

private suspend fun createUser(call: ApplicationCall, app: Application) {
    val connection = connect(false, app)

    connection.use {
        val params = call.receiveParameters()

        val username = params["username"]
        val password = params["password"]

        if (null === username || null === password) {
            call.respond(HttpStatusCode.BadRequest)
        }
        else {
            val service = UserService(it)

            service.createUser(username, password)

            call.respond(HttpStatusCode.OK)
        }
    }
}

private suspend fun getUser(call: ApplicationCall, app: Application) {
    val connection = connect(false, app)

    connection.use {
        val params = call.receiveParameters()

        val username = params["username"]
        val password = params["password"]

        if (null === username || null === password) {
            call.respond(HttpStatusCode.BadRequest)
        }
        else {
            val service = UserService(it)

            val user = service.getUserByLogin(username, password)

            if (null === user) {
                call.respond(HttpStatusCode.Unauthorized)
            }
            else {
                call.respond(HttpStatusCode.OK, user)
            }
        }
    }
}
