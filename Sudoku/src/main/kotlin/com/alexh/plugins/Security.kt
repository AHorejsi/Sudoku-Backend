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

            val verifierAlgorithm = Algorithm.HMAC256(jwtSecret)

            this.realm = config.property("jwt.realm").getString()

            this.verifier(
                JWT
                    .require(verifierAlgorithm)
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )

            this.validate { credential ->
                if (credential.payload.audience.contains(jwtAudience))
                    JWTPrincipal(credential.payload)
                else
                    null
            }
        }
    }
}
