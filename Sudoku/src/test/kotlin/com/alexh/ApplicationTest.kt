package com.alexh

import com.alexh.game.Difficulty
import com.alexh.game.Dimension
import com.alexh.game.Game
import com.alexh.models.GenerateRequest
import com.alexh.models.GenerateResponse
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
import io.ktor.client.*
import io.ktor.client.plugins.compression.*

class ApplicationTest {
    @Test
    fun testGenerate() = testApplication {
        this.createClient {
            this.install(ContentNegotiation) {
                this.json(Json {
                    this.prettyPrint = true
                    this.isLenient = true
                })
            }

            this.install(ContentEncoding) {
                this.gzip(1.0f)
            }

            this.install(Logging) {
                this.logger = Logger.DEFAULT
                this.level = LogLevel.ALL
            }
        }.use { client ->
            for (difficulty in Difficulty.values()) {
                this@ApplicationTest.testGenerateHelper1(client, Dimension.NINE, difficulty)
            }

            client.post(Endpoints.GENERATE) {
                this@ApplicationTest.setHeaders(this, XRequestIds.GENERATE)
            }
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

            val dimensionName = dimension.name
            val difficultyName = difficulty.name
            val gameNames = games.map { it.name }.toSet()
            val requestBody = GenerateRequest(dimensionName, difficultyName, gameNames)

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

    @Test
    fun testCrud() = testApplication {
        TODO()
    }

    private fun setHeaders(builder: HttpRequestBuilder, reqId: String) {
        builder.headers {
            this.append(HttpHeaders.XRequestId, reqId)
            this.append(HttpHeaders.ContentType, "application/json")
            this.append(HttpHeaders.ContentEncoding, "gzip")
            this.append(HttpHeaders.AcceptCharset, "ISO-8859-1")
            this.append(HttpHeaders.AcceptEncoding, "gzip, deflate, br")
            this.append(HttpHeaders.Connection, "keep-alive")
            this.append(HttpHeaders.AccessControlAllowOrigin, "*")
            this.append(HttpHeaders.UserAgent, "ApplicationTest")
        }
    }
}
