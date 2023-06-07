package com.alexh.plugins

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*

fun configureSecurity(app: Application) {
    app.authentication {
        this.jwt {
            val jwtAudience = app.environment.config.property("jwt.audience").getString()

            this.realm = app.environment.config.property("jwt.realm").getString()

            verifier(
                JWT
                    .require(Algorithm.HMAC256("secret"))
                    .withAudience(jwtAudience)
                    .withIssuer(app.environment.config.property("jwt.domain").getString())
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
