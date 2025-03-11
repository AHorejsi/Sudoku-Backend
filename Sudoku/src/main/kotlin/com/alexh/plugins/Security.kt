package com.alexh.plugins

import com.alexh.utils.JwtClaims
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun configureSecurity(app: Application) {
    app.install(Authentication) {
        this.jwt("auth-jwt") {
            val config = app.environment.config

            val jwtSecret = config.property("jwt.secret").getString()
            val jwtIssuer = config.property("jwt.issuer").getString()
            val jwtAudience = config.property("jwt.audience").getString()

            val verifierAlgorithm = Algorithm.HMAC256(jwtSecret)
            val expiration = System.currentTimeMillis() * 60000

            this.realm = config.property("jwt.realm").getString()

            this.verifier(
                JWT
                    .require(verifierAlgorithm)
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .withClaimPresence(JwtClaims.OP_KEY)
                    .acceptExpiresAt(expiration)
                    .build()
            )

            this.validate { credential ->
                val payload = credential.payload

                if (payload.audience.contains(jwtAudience) && payload.issuer == jwtIssuer)
                    JWTPrincipal(credential.payload)
                else
                    null
            }

            this.challenge { _, _ ->
                this.call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
