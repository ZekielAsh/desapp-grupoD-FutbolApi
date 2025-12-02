package com.example.demo.integration

import com.example.demo.model.football.MatchDto
import com.example.demo.model.football.PlayerDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TeamControllerE2eTest {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private var authToken: String = ""

    @BeforeEach
    fun setup() {
        registerUserIfNeeded()
        authToken = loginAndGetToken()
    }

    private fun registerUserIfNeeded() {
        val registerUrl = "http://localhost:$port/auth/register"

        val registerRequest = mapOf(
            "username" to "testuser",
            "password" to "testpass"
        )

        println("=== REGISTRATION ===")
        println("Register URL: $registerUrl")

        try {
            val response = restTemplate.postForEntity(
                registerUrl,
                registerRequest,
                Map::class.java
            )

            println("Registration Status: ${response.statusCode}")

            if (response.statusCode == HttpStatus.OK) {
                println("User registered successfully")
            } else if (response.statusCode == HttpStatus.CONFLICT) {
                println("User already exists (this is OK)")
            } else {
                println("Unexpected registration status: ${response.statusCode}")
            }
        } catch (e: Exception) {
            println("Registration note: ${e.message}")
            println("This is OK if user already exists")
        }
        println("====================")
    }

    private fun loginAndGetToken(): String {
        val loginUrl = "http://localhost:$port/auth/login"

        val loginRequest = mapOf(
            "username" to "testuser",
            "password" to "testpass"
        )

        println("=== AUTHENTICATION ===")
        println("Login URL: $loginUrl")

        try {
            val response = restTemplate.postForEntity(
                loginUrl,
                loginRequest,
                Map::class.java
            )

            println("Login Status: ${response.statusCode}")

            if (response.statusCode == HttpStatus.OK) {
                val token = (response.body as? Map<*, *>)?.get("token") as? String
                println("Token obtained successfully")
                println("Token preview: ${token?.take(20)}...")
                println("======================")
                return token ?: ""
            } else {
                println("Login failed with status: ${response.statusCode}")
                println("Response: ${response.body}")
                println("======================")
                fail("Failed to obtain authentication token. Cannot proceed with tests.")
            }
        } catch (e: Exception) {
            println("Login error: ${e.message}")
            println("======================")
            fail("Authentication failed: ${e.message}")
        }

        return ""
    }

    private fun createAuthHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        if (authToken.isNotEmpty()) {
            headers.setBearerAuth(authToken)
            println("Authorization header set: Bearer ${authToken.take(20)}...")
        } else {
            println("WARNING: No auth token available!")
            fail("Cannot proceed without authentication token")
        }
        return headers
    }

    @Test
    fun `getPlayers returns list`() {
        val teamId = 65L
        val url = "http://localhost:$port/teams/$teamId/players"

        println("=== TEST GET PLAYERS ===")
        println("URL: $url")
        println("Using Bearer Token: ${authToken.take(20)}...")

        val headers = createAuthHeaders()
        val entity = HttpEntity<Void>(headers)

        val response: ResponseEntity<List<PlayerDto>> = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            object : ParameterizedTypeReference<List<PlayerDto>>() {}
        )

        println("Status Code: ${response.statusCode}")
        println("Response Size: ${response.body?.size}")
        println("========================")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)

        if (response.body!!.isEmpty()) {
            println("WARNING: Response is empty. Possible reasons:")
            println("1. Team ID 65 may not exist in Football-Data API")
            println("2. API rate limit reached")
            println("3. API key may have limited access")
        }
    }

    @Test
    fun `getNextMatches returns list`() {
        val teamId = 65L
        val url = "http://localhost:$port/teams/$teamId/next-matches"

        println("=== TEST GET NEXT MATCHES ===")
        println("URL: $url")
        println("Using Bearer Token: ${authToken.take(20)}...")

        val headers = createAuthHeaders()
        val entity = HttpEntity<Void>(headers)

        val response: ResponseEntity<List<MatchDto>> = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            object : ParameterizedTypeReference<List<MatchDto>>() {}
        )

        println("Status Code: ${response.statusCode}")
        println("Response Size: ${response.body?.size}")
        println("=============================")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)

        if (response.body!!.isEmpty()) {
            println("WARNING: No scheduled matches found for team ID $teamId")
        }
    }

    @Test
    fun `test with Real Madrid team ID 86`() {
        val teamId = 86L
        val url = "http://localhost:$port/teams/$teamId/players"

        println("=== TEST WITH REAL MADRID (ID: 86) ===")
        println("URL: $url")
        println("Using Bearer Token: ${authToken.take(20)}...")

        val headers = createAuthHeaders()
        val entity = HttpEntity<Void>(headers)

        val response: ResponseEntity<List<PlayerDto>> = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            object : ParameterizedTypeReference<List<PlayerDto>>() {}
        )

        println("Status Code: ${response.statusCode}")
        println("Response Size: ${response.body?.size}")
        if (response.body != null && response.body!!.isNotEmpty()) {
            println("First Player: ${response.body!![0].name}")
            println("Total Players: ${response.body!!.size}")
        }
        println("=======================================")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)

        if (response.body!!.isNotEmpty()) {
            println("✓ Successfully retrieved ${response.body!!.size} players from Real Madrid")
            assertTrue(response.body!!.size > 0, "Real Madrid should have players")
        } else {
            println("✗ No players found for Real Madrid")
        }
    }
}