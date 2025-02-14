package com.alexh

import io.ktor.client.request.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.http.*
import com.alexh.route.configureRoutingForUsers
import com.alexh.utils.Cookies
import com.alexh.utils.Endpoints
import com.alexh.utils.FormFields
import io.ktor.client.request.forms.*

class ApplicationTest {
    @Test
    fun testGenerate() = testApplication {
        TODO()
    }

    @Test
    fun testUsers() = testApplication {
        this.application {
            configureRoutingForUsers(this)
        }

        this@ApplicationTest.createUser(this)
        this@ApplicationTest.getUserByUsername(this)
        this@ApplicationTest.deleteUserByEmail(this)

        this@ApplicationTest.createUser(this)
        this@ApplicationTest.getUserByEmail(this)
        this@ApplicationTest.deleteUserByUsername(this)
    }

    private suspend fun createUser(builder: ApplicationTestBuilder) {
        val response = builder.client.put(Endpoints.CREATE_USER) {
            this.header("Content-Type", "application/json")
            this.setBody(FormDataContent(Parameters.build {
                this.append(FormFields.USERNAME, "ahorejsi")
                this.append(FormFields.PASSWORD, "abcd")
                this.append(FormFields.EMAIL, "1111")
            }))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    private suspend fun getUserByUsername(builder:ApplicationTestBuilder) {
        val response = builder.client.get(Endpoints.GET_USER) {
            this.header("Content-Type", "application/json")
            this.setBody(FormDataContent(Parameters.build {
                this.append(FormFields.USERNAME_OR_EMAIL, "ahorejsi")
                this.append(FormFields.PASSWORD, "abcd")
            }))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    private suspend fun deleteUserByEmail(builder:ApplicationTestBuilder) {
        val response = builder.client.delete(Endpoints.DELETE_USER) {
            this.header("Content-Type", "application/json")
            this.cookie(Cookies.USER_ID, "1")
            this.setBody(FormDataContent(Parameters.build {
                this.append(FormFields.USERNAME_OR_EMAIL, "1111")
                this.append(FormFields.PASSWORD, "abcd")
            }))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    private suspend fun getUserByEmail(builder:ApplicationTestBuilder) {
        val response = builder.client.get(Endpoints.GET_USER) {
            this.header("Content-Type", "application/json")
            this.setBody(FormDataContent(Parameters.build {
                this.append(FormFields.USERNAME_OR_EMAIL, "1111")
                this.append(FormFields.PASSWORD, "abcd")
            }))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    private suspend fun deleteUserByUsername(builder:ApplicationTestBuilder) {
        val response = builder.client.delete(Endpoints.DELETE_USER) {
            this.header("Content-Type", "application/json")
            this.cookie(Cookies.USER_ID, "1")
            this.setBody(FormDataContent(Parameters.build {
                this.append(FormFields.USERNAME_OR_EMAIL, "ahorejsi")
                this.append(FormFields.PASSWORD, "abcd")
            }))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testPuzzles() = testApplication {
        TODO()
    }
}
