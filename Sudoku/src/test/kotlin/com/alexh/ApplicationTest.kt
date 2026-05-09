package com.alexh

import com.alexh.game.Difficulty
import com.alexh.game.Dimension
import com.alexh.game.Game
import com.alexh.models.*
import com.alexh.route.createJwtToken
import com.alexh.utils.Endpoints
import com.alexh.utils.XRequestIds
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ApplicationTest {
    private val successfulUserId = 1
    private val successfulPuzzleId = 1
    private val successfulUsername = "ah15"
    private val successfulPassword = "3123AsDf!@#$"
    private val successfulEmail = "ah15@test.com"
    private val invalidUserId = -1
    private val invalidPuzzleId = -1
    private val invalidUsername = ""
    private val invalidPassword = "3123"
    private val invalidEmail = "ah15@test"
    private val updatedUsername = "jt27"
    private val updatedEmail = "jt27@try.org"

    @Test
    fun testGenerate() = testApplication {
        this.createClient {
            this@ApplicationTest.installJson(this)
            this@ApplicationTest.installLogging(this)
            this@ApplicationTest.installCompression(this)
        }.use { client ->
            runBlocking(Dispatchers.IO) {
                this.launch { this@ApplicationTest.testGenerateHelper0(client, this) }
                this.launch { this@ApplicationTest.testUnfilledFieldsOnGenerate(client) }
            }
        }
    }

    private fun testGenerateHelper0(client: HttpClient, scope: CoroutineScope) {
        scope.launch {
            val testCount = 5

            repeat(testCount) {
                this@ApplicationTest.testGenerateHelper1(client)
            }
        }
    }

    private suspend fun testGenerateHelper1(client: HttpClient) {
        for (dimension in Dimension.values()) {
            for (difficulty in Difficulty.values()) {
                this.testGenerateHelper2(client, dimension, difficulty)
            }
        }
    }

    private suspend fun testGenerateHelper2(
        client: HttpClient,
        dimension: Dimension,
        difficulty: Difficulty
    ) {
        val games = Game.values()

        for (startIndex in games.indices) {
            for (endIndex in startIndex .. games.size) {
                val selectedGames = games.sliceArray(startIndex until endIndex).toSet()

                this.testGenerateHelper3(client, dimension, difficulty, selectedGames)
            }
        }
    }

    private suspend fun testGenerateHelper3(
        client: HttpClient,
        dimension: Dimension,
        difficulty: Difficulty,
        games: Set<Game>
    ) {
        client.post(Endpoints.GENERATE) {
            this@ApplicationTest.setJwtHeaders(this, XRequestIds.GENERATE, this@ApplicationTest.successfulUsername)

            val dimensionName = dimension.name
            val difficultyName = difficulty.name
            val gameNames = games.map { it.name }.toSet()

            val requestBody = GenerateRequest(dimensionName, difficultyName, gameNames)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = response.body<GenerateResponse>()
            assertIs<GenerateResponse.Success>(responseBody)

            val json = responseBody.sudoku
            assertEquals(dimension.length, json.length)
            assertEquals(difficulty, json.difficulty)
            assertEquals(games, json.games)
        }
    }

    private suspend fun testUnfilledFieldsOnGenerate(client: HttpClient) {
        val response = client.post(Endpoints.GENERATE) {
            this@ApplicationTest.setJwtHeaders(this, XRequestIds.GENERATE, this@ApplicationTest.successfulEmail)

            val dimensionName = ""
            val difficultyName = ""
            val gameNames = emptySet<String>()

            val requestBody = GenerateRequest(dimensionName, difficultyName, gameNames)

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
            this@ApplicationTest.testUpdateUser(client)
            this@ApplicationTest.testCreatePuzzle(client)
            this@ApplicationTest.testUpdatePuzzle(client)
            this@ApplicationTest.testDeletePuzzle(client)
            this@ApplicationTest.testDeleteUser(client)
        }
    }

    private suspend fun testCreateUser(client: HttpClient) {
        this.attemptToCreateUser(
            client,
            CreateUserResponse.Success::class.simpleName!!,
            this.successfulUsername,
            this.successfulPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.DuplicateFound::class.simpleName!!,
            this.successfulUsername,
            this.successfulPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.InvalidUsername::class.simpleName!!,
            this.invalidUsername,
            this.successfulPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.InvalidPassword::class.simpleName!!,
            this.successfulUsername,
            this.invalidPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.InvalidEmail::class.simpleName!!,
            this.successfulUsername,
            this.successfulPassword,
            this.invalidEmail
        )
    }

    private suspend fun attemptToCreateUser(
        client: HttpClient,
        expectedResponse: String,
        username: String,
        password: String,
        email: String
    ) {
        client.put(Endpoints.CREATE_USER) {
            this@ApplicationTest.setStandardHeaders(this, XRequestIds.CREATE_USER)

            val requestBody = CreateUserRequest(username, password, email)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val actualResponse = response.body<CreateUserResponse>()::class.simpleName!!
            assertEquals(expectedResponse, actualResponse)
        }
    }

    private suspend fun testReadUser(client: HttpClient) {
        this.attemptToReadUserWithSuccess(client)
        this.attemptToReadUserWithFailure(client)
    }

    private suspend fun attemptToReadUserWithSuccess(client: HttpClient) {
        client.post(Endpoints.READ_USER) {
            this@ApplicationTest.setStandardHeaders(this, XRequestIds.READ_USER)

            val requestBody = ReadUserRequest(
                this@ApplicationTest.successfulUsername,
                this@ApplicationTest.successfulPassword,
            )

            this.setBody(requestBody)
        }.also { response ->
            this.checkSuccessfulReadUser(response)
        }

        client.post(Endpoints.READ_USER) {
            this@ApplicationTest.setStandardHeaders(this, XRequestIds.READ_USER)

            val requestBody = ReadUserRequest(
                this@ApplicationTest.successfulEmail,
                this@ApplicationTest.successfulPassword,
            )

            this.setBody(requestBody)
        }.also { response ->
            this.checkSuccessfulReadUser(response)
        }
    }

    private suspend fun checkSuccessfulReadUser(response: HttpResponse) {
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.body<ReadUserResponse>()
        assertIs<ReadUserResponse.Success>(responseBody)

        val user = responseBody.user
        assertEquals(this.successfulUserId, user.id)
        assertEquals(this.successfulUsername, user.username)
        assertEquals(this.successfulEmail, user.email)
        assertEquals(0, user.puzzles.size)
    }

    private suspend fun attemptToReadUserWithFailure(client: HttpClient) {
        client.post(Endpoints.READ_USER) {
            this@ApplicationTest.setStandardHeaders(this, XRequestIds.READ_USER)

            val requestBody = ReadUserRequest(
                this@ApplicationTest.invalidUsername,
                this@ApplicationTest.invalidPassword
            )

            this.setBody(requestBody)
        }.also { response ->
            this.checkFailedReadUser(response)
        }

        client.post(Endpoints.READ_USER) {
            this@ApplicationTest.setStandardHeaders(this, XRequestIds.READ_USER)

            val requestBody = ReadUserRequest(
                this@ApplicationTest.invalidEmail,
                this@ApplicationTest.invalidPassword
            )

            this.setBody(requestBody)
        }.also { response ->
            this.checkFailedReadUser(response)
        }
    }

    private suspend fun checkFailedReadUser(response: HttpResponse) {
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.body<ReadUserResponse>()
        assertIs<ReadUserResponse.FailedToFind>(responseBody)
    }

    private suspend fun testUpdateUser(client: HttpClient) {
        this.attemptToUpdateUser(
            client,
            UpdateUserResponse.InvalidUsername::class.simpleName!!,
            this.invalidUsername,
            this.updatedEmail
        )
        this.attemptToUpdateUser(
            client,
            UpdateUserResponse.InvalidEmail::class.simpleName!!,
            this.updatedUsername,
            this.invalidEmail
        )
        this.attemptToUpdateUser(
            client,
            UpdateUserResponse.Success::class.simpleName!!,
            this.updatedUsername,
            this.updatedEmail
        )
    }

    private suspend fun attemptToUpdateUser(
        client: HttpClient,
        expectedResponse: String,
        newUsername: String,
        newEmail: String
    ) {
        client.put(Endpoints.UPDATE_USER) {
            this@ApplicationTest.setJwtHeaders(this, XRequestIds.UPDATE_USER, this@ApplicationTest.successfulUsername)

            val requestBody = UpdateUserRequest(
                this@ApplicationTest.successfulUserId,
                newUsername,
                newEmail
            )

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val actualResponse = response.body<UpdateUserResponse>()::class.simpleName!!
            assertEquals(expectedResponse, actualResponse)
        }
    }

    private suspend fun testCreatePuzzle(client: HttpClient) {
        client.put(Endpoints.CREATE_PUZZLE) {
            this@ApplicationTest.setJwtHeaders(this, XRequestIds.CREATE_PUZZLE, this@ApplicationTest.successfulUsername)

            val requestBody = CreatePuzzleRequest("{}", this@ApplicationTest.successfulUserId)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = response.body<CreatePuzzleResponse>()
            assertIs<CreatePuzzleResponse.Success>(responseBody)
        }
    }

    private suspend fun testUpdatePuzzle(client: HttpClient) {
        val fakeJson = "{\"puzzle\": {}}"

        this.attemptToUpdatePuzzle(
            client,
            this.successfulPuzzleId,
            fakeJson,
            UpdatePuzzleResponse.Success::class.simpleName!!
        )
        this.attemptToUpdatePuzzle(
            client,
            this.invalidPuzzleId,
            fakeJson,
            UpdatePuzzleResponse.FailedToFind::class.simpleName!!
        )
    }

    private suspend fun attemptToUpdatePuzzle(
        client: HttpClient,
        puzzleId: Int,
        json: String,
        expectedResponse: String
    ) {
        client.put(Endpoints.UPDATE_PUZZLE) {
            this@ApplicationTest.setJwtHeaders(this, XRequestIds.UPDATE_PUZZLE, this@ApplicationTest.successfulEmail)

            val requestBody = UpdatePuzzleRequest(puzzleId, json)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val actualResponse = response.body<UpdatePuzzleResponse>()::class.simpleName!!
            assertEquals(expectedResponse, actualResponse)
        }
    }

    private suspend fun testDeletePuzzle(client: HttpClient) {
        this.attemptToDeletePuzzle(
            client,
            this.successfulPuzzleId,
            DeletePuzzleResponse.Success::class.simpleName!!
        )
        this.attemptToDeletePuzzle(
            client,
            this.invalidPuzzleId,
            DeletePuzzleResponse.FailedToFind::class.simpleName!!
        )
    }

    private suspend fun attemptToDeletePuzzle(
        client: HttpClient,
        puzzleId: Int,
        expectedResponse: String
    ) {
        client.delete(Endpoints.DELETE_PUZZLE) {
            this@ApplicationTest.setJwtHeaders(this, XRequestIds.DELETE_PUZZLE, this@ApplicationTest.successfulUsername)

            val requestBody = DeletePuzzleRequest(puzzleId)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val actualResponse = response.body<DeletePuzzleResponse>()::class.simpleName!!
            assertEquals(expectedResponse, actualResponse)
        }
    }

    private suspend fun testDeleteUser(client: HttpClient) {
        this.attemptToDeleteUser(
            client,
            this.successfulUserId,
            DeleteUserResponse.Success::class.simpleName!!
        )
        this.attemptToDeleteUser(
            client,
            this.invalidUserId,
            DeleteUserResponse.FailedToFind::class.simpleName!!
        )
    }

    private suspend fun attemptToDeleteUser(
        client: HttpClient,
        userId: Int,
        expectedResponse: String
    ) {
        client.delete(Endpoints.DELETE_USER) {
            this@ApplicationTest.setJwtHeaders(this, XRequestIds.DELETE_USER, this@ApplicationTest.successfulEmail)

            val requestBody = DeleteUserRequest(userId)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val actualResponse = response.body<DeleteUserResponse>()::class.simpleName!!
            assertEquals(expectedResponse, actualResponse)
        }
    }

    private fun setJwtHeaders(builder: HttpRequestBuilder, xReqId: String, usernameOrEmail: String) {
        val jwtToken = createJwtToken(usernameOrEmail)

        builder.headers.append(HttpHeaders.Authorization, "Bearer $jwtToken")

        this.setStandardHeaders(builder, xReqId)
    }

    private fun setStandardHeaders(builder: HttpRequestBuilder, xReqId: String) {
        builder.headers {
            this.append(HttpHeaders.XRequestId, xReqId)
            this.append(HttpHeaders.AcceptEncoding, "gzip")
            this.append(HttpHeaders.AcceptCharset, "ISO-8859-1")
            this.append(HttpHeaders.AccessControlAllowOrigin, "*")
            this.append(HttpHeaders.Allow, "OPTIONS, GET, POST, PUT, DELETE")
            this.append(HttpHeaders.Connection, "keep-alive")
            this.append(HttpHeaders.ContentType, "application/json")
            this.append(HttpHeaders.ContentEncoding, "gzip")
            this.append(HttpHeaders.UserAgent, "Mozilla/5.0")
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

    private fun installCompression(config: HttpClientConfig<*>) {
        config.install(ContentEncoding) {
            this.gzip(1.0f)
        }
    }
}
