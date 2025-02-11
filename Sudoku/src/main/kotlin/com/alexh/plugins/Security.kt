package com.alexh.plugins

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*

fun configureSecurity(app: Application) {
    app.install(Authentication) {
        this.jwt("auth-jwt") {
            val config = app.environment.config

            val jwtSecret = config.property("jwt.secret").getString()
            val jwtAudience = config.property("jwt.audience").getString()
            val jwtIssuer = config.property("jwt.issuer").getString()

            this.realm = config.property("jwt.realm").getString()

            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience))
                    JWTPrincipal(credential.payload)
                else
                    null
            }
        }
    }
}
