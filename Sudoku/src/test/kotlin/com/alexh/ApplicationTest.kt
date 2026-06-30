package com.alexh

import com.alexh.asserts.assertGreater
import com.alexh.game.*
import com.alexh.models.*
import com.alexh.route.createJwtToken
import com.alexh.utils.*
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

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
    fun testHealthCheck(): Unit = testApplication {
        this.createClient {
            this@ApplicationTest.installJson(this)
            this@ApplicationTest.installLogging(this)
        }.use { client ->
            client.get(Endpoints.PING).also { response ->
                assertEquals(HttpStatusCode.OK, response.status)

                val message = response.bodyAsText()
                assertEquals(StatusMessages.HEALTH_CHECK, message)
            }
        }
    }

    @Test
    fun testGenerate(): Unit = testApplication {
        this.createClient {
            this@ApplicationTest.installJson(this)
            this@ApplicationTest.installLogging(this)
            this@ApplicationTest.installCompression(this)
        }.use { client ->
            this@ApplicationTest.testGenerateHelper0(client)
            this@ApplicationTest.testUnfilledFieldsOnGenerate(client)
        }
    }

    private suspend fun testGenerateHelper0(client: HttpClient) {
        val dimensionArray = Dimension.states
        val difficultyArray = Difficulty.states
        val gameSubsets = Game.subsets

        for (dimension in dimensionArray) {
            for (difficulty in difficultyArray) {
                for (games in gameSubsets) {
                    this.testGenerateHelper1(client, dimension, difficulty, games)
                }
            }
        }
    }

    private suspend fun testGenerateHelper1(
        client: HttpClient,
        dimension: Dimension,
        difficulty: Difficulty,
        games: Set<Game>
    ) {
        client.post(Endpoints.GENERATE) {
            this@ApplicationTest.setJwtHeaders(
                this,
                XRequestIds.GENERATE,
                this@ApplicationTest.successfulUsername
            )

            val dimensionName = dimension.name
            val difficultyName = difficulty.name
            val gameNames = games.map(Game::name).toSet()

            val requestBody = GenerateRequest(dimensionName, difficultyName, gameNames)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = response.body<GenerateResponse>()
            assertIs<GenerateResponse.Success>(responseBody)

            val info = responseBody.sudoku.description
            assertEquals(dimension, info.dimension)
            assertEquals(difficulty, info.difficulty)
            assertEquals(games, info.games)
        }
    }

    private suspend fun testUnfilledFieldsOnGenerate(client: HttpClient) {
        client.post(Endpoints.GENERATE) {
            this@ApplicationTest.setJwtHeaders(
                this,
                XRequestIds.GENERATE,
                this@ApplicationTest.successfulEmail
            )

            val dimensionName = ""
            val difficultyName = ""
            val gameNames = emptySet<String>()

            val requestBody = GenerateRequest(dimensionName, difficultyName, gameNames)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = response.body<GenerateResponse>()
            assertIs<GenerateResponse.UnfilledFields>(responseBody)
        }
    }

    @Test
    fun testDailySudoku(): Unit = testApplication {
        this.createClient {
            this@ApplicationTest.installJson(this)
            this@ApplicationTest.installLogging(this)
            this@ApplicationTest.installCompression(this)
        }.use { client ->
            this@ApplicationTest.testGetDaily(client)
        }
    }

    private suspend fun testGetDaily(client: HttpClient) {
        for (dimension in Dimension.states) {
            for (difficulty in Difficulty.states) {
                for (games in Game.subsets) {
                    this.testGetDailySudokuWithSettings(client, dimension, difficulty, games)
                }
            }
        }
    }

    private suspend fun testGetDailySudokuWithSettings(
        client: HttpClient,
        dimension: Dimension,
        difficulty: Difficulty,
        games: Set<Game>
    ) {
        client.post(Endpoints.DAILY) {
            this@ApplicationTest.setJwtHeaders(
                this,
                XRequestIds.DAILY,
                this@ApplicationTest.successfulUsername
            )

            val dimensionName = dimension.name
            val difficultyName = difficulty.name
            val gameNames = games.map(Game::name).toSet()

            val requestBody = GenerateRequest(dimensionName, difficultyName, gameNames)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = response.body<GenerateResponse>()
            assertIs<GenerateResponse.Success>(responseBody)
        }
    }

    @Test
    fun testUserCrud(): Unit = testApplication {
        this.createClient {
            this@ApplicationTest.installJson(this)
            this@ApplicationTest.installCompression(this)
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
            CreateUserResponse.Success.typeName(),
            this.successfulUsername,
            this.successfulPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.DuplicateFound.typeName(),
            this.successfulUsername,
            this.successfulPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.InvalidUsername.typeName(),
            this.invalidUsername,
            this.successfulPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.InvalidPassword.typeName(),
            this.successfulUsername,
            this.invalidPassword,
            this.successfulEmail
        )
        this.attemptToCreateUser(
            client,
            CreateUserResponse.InvalidEmail.typeName(),
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

            val actualResponse = response.body<CreateUserResponse>().typeName()
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
            UpdateUserResponse.InvalidUsername.typeName(),
            this.invalidUsername,
            this.updatedEmail
        )
        this.attemptToUpdateUser(
            client,
            UpdateUserResponse.InvalidEmail.typeName(),
            this.updatedUsername,
            this.invalidEmail
        )
        this.attemptToUpdateUser(
            client,
            UpdateUserResponse.Success.typeName(),
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
            this@ApplicationTest.setJwtHeaders(
                this,
                XRequestIds.UPDATE_USER,
                this@ApplicationTest.successfulUsername
            )

            val requestBody = UpdateUserRequest(
                this@ApplicationTest.successfulUserId,
                newUsername,
                newEmail
            )

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val actualResponse = response.body<UpdateUserResponse>().typeName()
            assertEquals(expectedResponse, actualResponse)
        }
    }

    private suspend fun testCreatePuzzle(client: HttpClient) {
        val defaultDimension = Dimension.NINE
        val defaultDifficulty = Difficulty.BEGINNER
        val defaultGames = emptySet<Game>()

        client.put(Endpoints.CREATE_PUZZLE) {
            this@ApplicationTest.setJwtHeaders(
                this,
                XRequestIds.CREATE_PUZZLE,
                this@ApplicationTest.successfulUsername
            )

            val info = MakeSudokuCommand(defaultDimension, defaultDifficulty, defaultGames)
            val sudoku = makeSudoku(info)
            val json = Json.encodeToString(sudoku)

            val requestBody = CreatePuzzleRequest(json, this@ApplicationTest.successfulUserId)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val responseBody = response.body<CreatePuzzleResponse>()
            assertIs<CreatePuzzleResponse.Success>(responseBody)

            val puzzle = responseBody.puzzle
            val json = Json.decodeFromString<SudokuJson>(puzzle.json)

            assertGreater(puzzle.id, 0)

            val description = json.description

            assertEquals(defaultDimension, description.dimension)
            assertEquals(defaultDifficulty, description.difficulty)
            assertEquals(defaultGames, description.games)

            assertNull(json.cages)
            assertFalse(json.boxes.any(Box::isHyper))
        }
    }

    private suspend fun testUpdatePuzzle(client: HttpClient) {
        val successInfo = MakeSudokuCommand(Dimension.NINE, Difficulty.MASTER, Game.states.toSet())
        val successSudoku = makeSudoku(successInfo)
        val successJson = Json.encodeToString(successSudoku)
        this.attemptToUpdatePuzzle(
            client,
            this.successfulPuzzleId,
            successJson,
            UpdatePuzzleResponse.Success.typeName()
        )

        val failureInfo = MakeSudokuCommand(Dimension.NINE, Difficulty.MEDIUM, setOf(Game.KILLER))
        val failureSudoku = makeSudoku(failureInfo)
        val failureJson = Json.encodeToString(failureSudoku)
        this.attemptToUpdatePuzzle(
            client,
            this.invalidPuzzleId,
            failureJson,
            UpdatePuzzleResponse.FailedToFind.typeName()
        )
    }

    private suspend fun attemptToUpdatePuzzle(
        client: HttpClient,
        puzzleId: Int,
        json: String,
        expectedResponse: String
    ) {
        client.put(Endpoints.UPDATE_PUZZLE) {
            this@ApplicationTest.setJwtHeaders(
                this,
                XRequestIds.UPDATE_PUZZLE,
                this@ApplicationTest.successfulEmail
            )

            val requestBody = UpdatePuzzleRequest(puzzleId, json)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val actualResponse = response.body<UpdatePuzzleResponse>().typeName()
            assertEquals(expectedResponse, actualResponse)
        }
    }

    private suspend fun testDeletePuzzle(client: HttpClient) {
        this.attemptToDeletePuzzle(
            client,
            this.successfulPuzzleId,
            DeletePuzzleResponse.Success.typeName()
        )
        this.attemptToDeletePuzzle(
            client,
            this.invalidPuzzleId,
            DeletePuzzleResponse.FailedToFind.typeName()
        )
    }

    private suspend fun attemptToDeletePuzzle(
        client: HttpClient,
        puzzleId: Int,
        expectedResponse: String
    ) {
        client.delete(Endpoints.DELETE_PUZZLE) {
            this@ApplicationTest.setJwtHeaders(
                this,
                XRequestIds.DELETE_PUZZLE,
                this@ApplicationTest.successfulUsername
            )

            val requestBody = DeletePuzzleRequest(puzzleId)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val actualResponse = response.body<DeletePuzzleResponse>().typeName()
            assertEquals(expectedResponse, actualResponse)
        }
    }

    private suspend fun testDeleteUser(client: HttpClient) {
        this.attemptToDeleteUser(
            client,
            this.successfulUserId,
            DeleteUserResponse.Success.typeName()
        )
        this.attemptToDeleteUser(
            client,
            this.invalidUserId,
            DeleteUserResponse.FailedToFind.typeName()
        )
    }

    private suspend fun attemptToDeleteUser(
        client: HttpClient,
        userId: Int,
        expectedResponse: String
    ) {
        client.delete(Endpoints.DELETE_USER) {
            this@ApplicationTest.setJwtHeaders(
                this,
                XRequestIds.DELETE_USER,
                this@ApplicationTest.successfulEmail
            )

            val requestBody = DeleteUserRequest(userId)

            this.setBody(requestBody)
        }.also { response ->
            assertEquals(HttpStatusCode.OK, response.status)

            val actualResponse = response.body<DeleteUserResponse>().typeName()
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
