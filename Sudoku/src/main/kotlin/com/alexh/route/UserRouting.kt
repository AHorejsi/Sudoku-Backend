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

private const val useEmbeddedDatabase = true

fun configureRoutingForUsers(app: Application) {
    app.routing {
        this.post(Endpoints.CREATE_USER_LOGIN) {
            createUser(app, this.call)
        }
        this.get(Endpoints.GET_USER_BY_LOGIN) {
            getUser(app, this.call)
        }
        this.post(Endpoints.DELETE_USER_BY_LOGIN) {
            deleteUser(app, this.call)
        }
    }
}

private suspend fun createUser(app: Application, call: ApplicationCall) {
    val cookies = call.request.cookies

    val username = cookies[Cookies.USERNAME]
    val email = cookies[Cookies.EMAIL]
    val password = cookies[Cookies.PASSWORD]

    if (username.isNullOrEmpty() || email.isNullOrEmpty() || password.isNullOrEmpty()) {
        loginError()
    }

    val db = connect(useEmbeddedDatabase, app)
    val service = UserService(db)

    service.create(username, password, email)
}

private suspend fun getUser(app: Application, call: ApplicationCall) {
    val cookies = call.request.cookies

    val usernameOrEmail = cookies[Cookies.USERNAME_OR_EMAIL]
    val password = cookies[Cookies.PASSWORD]

    if (usernameOrEmail.isNullOrEmpty() || password.isNullOrEmpty()) {
        loginError()
    }

    val db = connect(useEmbeddedDatabase, app)
    val service = UserService(db)

    val login = service.read(usernameOrEmail, password)

    call.respond(HttpStatusCode.OK, login)
}

private suspend fun deleteUser(app: Application, call: ApplicationCall) {
    val user = call.receive<User>()

    val db = connect(useEmbeddedDatabase, app)
    val service = UserService(db)

    service.delete(user)
}

private fun loginError(): Nothing {
    throw InternalError("Invalid login information")
}
