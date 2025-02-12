package com.alexh

import io.ktor.client.request.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.http.*
import com.alexh.route.configureRoutingForGeneratingPuzzles
import com.alexh.utils.Endpoints

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        this.application {
            configureRoutingForGeneratingPuzzles(this)
        }

        this.client.get(Endpoints.GENERATION) {
            this.cookie("dimension", "NINE")
            this.cookie("difficulty", "MASTER")
            this.cookie("games", "HYPER,KILLER")
        }.apply {
            assertEquals(HttpStatusCode.OK, this.status)
        }
    }
}
