package com.alexh

import com.alexh.game.Difficulty
import com.alexh.game.Dimension
import com.alexh.game.Game
import com.alexh.models.*
import com.alexh.utils.Endpoints
import com.alexh.utils.XRequestIds
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import io.ktor.client.plugins.compression.*
import io.ktor.client.statement.*
import kotlin.reflect.KClass
import kotlin.test.assertTrue

class ApplicationTest {
    private val successfulUsername = "ah15"
    private val successfulPassword = "3123AsDf!@#$"
    private val successfulEmail = "ah15@test.com"
    private val invalidUsername = ""
    private val invalidPassword = "3123"
    private val invalidEmail = "ah15@test"

    @Test
    fun testGenerate() = testApplication {
        this.createClient {
            this@ApplicationTest.installJson(this)
            this@ApplicationTest.installLogging(this)

            this.install(ContentEncoding) {
                this.gzip(1.0f)
            }
        }.use { client ->
            for (difficulty in Difficulty.values()) {
                this@ApplicationTest.testGenerateHelper1(client, Dimension.NINE, difficulty)
            }

            this@ApplicationTest.testUnfilledFieldsOnGenerate(client)
        }
    }

    private suspend fun testGenerateHelper1(client: HttpClient, dimension: Dimension, difficulty: Difficulty) {
        val games = Game.values()

        for (startIndex in games.indices) {
            for (endIndex in startIndex..games.size) {
                val gameSet = games.slice(startIndex until endIndex).toSet()

                this.testGenerateHelper2(client, dimension, difficulty, gameSet)
            }
        }
    }

    private suspend fun testGenerateHelper2(
        client: HttpClient,
        dimension: Dimension,
        difficulty: Difficulty,
        games: Set<Game>
    ) {
        val response = client.post(Endpoints.GENERATE) {
            this@ApplicationTest.setHeaders(this, XRequestIds.GENERATE)

            val requestBody = GenerateRequest(dimension.name, difficulty.name, games.map{ it.name }.toSet())

            this.setBody(requestBody)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.body<GenerateResponse>()
        assertIs<GenerateResponse.Success>(responseBody)

        val puzzle = responseBody.puzzle
        assertEquals(dimension.length, puzzle.length)
        assertEquals(difficulty, puzzle.difficulty)
        assertEquals(games, puzzle.games)
    }

    private suspend fun testUnfilledFieldsOnGenerate(client: HttpClient) {
        val response = client.post(Endpoints.GENERATE) {
            this@ApplicationTest.setHeaders(this, XRequestIds.GENERATE)

            val requestBody = GenerateRequest("", "", emptySet())

            this.setBody(requestBody)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.body<GenerateResponse>()
        assertIs<GenerateResponse.UnfilledFields>(responseBody)
    }

    @Test
    fun testUserCrud() = testApplication {
        this.createClient {
            this@ApplicationTest.installJson(this)
            this@ApplicationTest.installLogging(this)
        }.use { client ->
            this@ApplicationTest.testCreateUser(client)
            this@ApplicationTest.testReadUser(client)

            // TODO: Test other CRUD operations
        }
    }

    private suspend fun testCreateUser(client: HttpClient) {
        this.attemptToCreateUser(
            client,
            CreateUserResponse.Success::class,
            this.successfulUsername,
            this.successfulPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.DuplicateFound::class,
            this.successfulUsername,
            this.successfulPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.InvalidUsername::class,
            this.invalidUsername,
            this.successfulPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.InvalidPassword::class,
            this.successfulUsername,
            this.invalidPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.InvalidEmail::class,
            this.successfulUsername,
            this.successfulPassword,
            this.invalidEmail
        )
    }

    private suspend fun attemptToCreateUser(
        client: HttpClient,
        cls: KClass<*>,
        username: String,
        password: String,
        email: String
    ) {
        val response = client.put(Endpoints.CREATE_USER) {
            this@ApplicationTest.setHeaders(this, XRequestIds.CREATE_USER)

            val requestBody = CreateUserRequest(username, password, email)

            this.setBody(requestBody)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.body<CreateUserResponse>()
        assertEquals(cls, responseBody::class)
    }

    private suspend fun testReadUser(client: HttpClient) {
        this.attemptToReadUserWithSuccess(client)
        this.attemptToReadUserWithFailure(client)
    }

    private suspend fun attemptToReadUserWithSuccess(client: HttpClient) {
        val responseWithUsername = client.post(Endpoints.READ_USER) {
            this@ApplicationTest.setHeaders(this, XRequestIds.READ_USER)

            val requestBody = ReadUserRequest(
                this@ApplicationTest.successfulUsername,
                this@ApplicationTest.successfulPassword,
            )

            this.setBody(requestBody)
        }
        val responseWithEmail = client.post(Endpoints.READ_USER) {
            this@ApplicationTest.setHeaders(this, XRequestIds.READ_USER)

            val requestBody = ReadUserRequest(
                this@ApplicationTest.successfulEmail,
                this@ApplicationTest.successfulPassword,
            )

            this.setBody(requestBody)
        }

        this.checkSuccessfulReadUser(responseWithUsername)
        this.checkSuccessfulReadUser(responseWithEmail)
    }

    private suspend fun checkSuccessfulReadUser(response: HttpResponse) {
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.body<ReadUserResponse>()
        assertIs<ReadUserResponse.Success>(responseBody)

        val user = responseBody.user
        assertTrue(user.id > 0)
        assertEquals(this.successfulUsername, user.username)
        assertEquals(this.successfulEmail, user.email)
        assertEquals(0, user.puzzles.size)
    }

    private suspend fun attemptToReadUserWithFailure(client: HttpClient) {
        val responseWithUsername = client.post(Endpoints.READ_USER) {
            this@ApplicationTest.setHeaders(this, XRequestIds.READ_USER)

            val requestBody = ReadUserRequest(
                this@ApplicationTest.invalidUsername,
                this@ApplicationTest.invalidPassword
            )

            this.setBody(requestBody)
        }
        val responseWithEmail = client.post(Endpoints.READ_USER) {
            this@ApplicationTest.setHeaders(this, XRequestIds.READ_USER)

            val requestBody = ReadUserRequest(
                this@ApplicationTest.invalidEmail,
                this@ApplicationTest.invalidPassword
            )

            this.setBody(requestBody)
        }

        this.checkFailedReadUser(responseWithUsername)
        this.checkFailedReadUser(responseWithEmail)
    }

    private suspend fun checkFailedReadUser(response: HttpResponse) {
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.body<ReadUserResponse>()
        assertIs<ReadUserResponse.FailedToFind>(responseBody)
    }

    private fun setHeaders(builder: HttpRequestBuilder, xReqId: String) {
        builder.headers {
            this.append(HttpHeaders.XRequestId, xReqId)
            this.append(HttpHeaders.ContentType, "application/json")
            this.append(HttpHeaders.ContentEncoding, "gzip")
            this.append(HttpHeaders.AcceptCharset, "ISO-8859-1")
            this.append(HttpHeaders.AcceptEncoding, "gzip, deflate, br")
            this.append(HttpHeaders.Connection, "keep-alive")
            this.append(HttpHeaders.AccessControlAllowOrigin, "*")
            this.append(HttpHeaders.UserAgent, "ApplicationTest")
        }
    }

    private fun installJson(config: HttpClientConfig<*>) {
        config.install(ContentNegotiation) {
            this.json(Json {
                this.prettyPrint = true
                this.isLenient = true
            })
        }
    }

    private fun installLogging(config: HttpClientConfig<*>) {
        config.install(Logging) {
            this.logger = Logger.DEFAULT
            this.level = LogLevel.ALL
        }
    }
}
