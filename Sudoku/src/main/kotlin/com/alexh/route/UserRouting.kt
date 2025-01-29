package com.alexh.route

import com.alexh.models.User
import com.alexh.models.UserService
import com.alexh.plugins.connect
import com.alexh.utils.Cookies
import com.alexh.utils.Endpoints
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun configureRoutingForUsers(app: Application) {
    app.routing {
        this.post(Endpoints.CREATE_USER_LOGIN) {
            createUser(app, this.call)
        }
        this.get(Endpoints.GET_USER_BY_LOGIN) {
            readUser(app, this.call)
        }
        this.post(Endpoints.DELETE_USER_BY_ID) {
            deleteUser(app, this.call)
        }
    }
}

private suspend fun createUser(app: Application, call: ApplicationCall) {
    val cookies = call.request.cookies

    val name = cookies[Cookies.NAME]
    val email = cookies[Cookies.EMAIL]
    val password = cookies[Cookies.PASSWORD]

    if (null === name || null === email || null === password) {
        throw InternalError("Necessary cookies have not been supplied")
    }

    val conn = connect(true, app)
    val service = UserService(conn)

    val result = service.create(name, email, password)

    if (null === result) {
        call.respond(HttpStatusCode.Conflict)
    }
    else {
        call.respond(HttpStatusCode.Created)
    }
}

private suspend fun readUser(app: Application, call: ApplicationCall) {
    val cookies = call.request.cookies

    val nameOrEmail = cookies[Cookies.NAME_OR_EMAIL]
    val password = cookies[Cookies.PASSWORD]

    if (null === nameOrEmail || null === password) {
        cookieError()
    }

    val conn = connect(true, app)
    val service = UserService(conn)

    val user = service.read(nameOrEmail, password)

    if (null === user) {
        call.respond(HttpStatusCode.Unauthorized)
    }
    else {
        call.respond(HttpStatusCode.OK, user)
    }
}

private suspend fun deleteUser(app: Application, call: ApplicationCall) {
    val user = call.receive<User>()

    val conn = connect(true, app)
    val service = UserService(conn)

    service.delete(user)

    call.respond(HttpStatusCode.OK)
}

private fun cookieError(): Nothing =
    throw InternalError("Necessary cookies have not been supplied")
