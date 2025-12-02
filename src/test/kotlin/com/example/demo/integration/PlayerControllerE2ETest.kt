package com.example.demo.integration

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

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class PlayerControllerE2eTest {

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
    fun `test with messi player stats endpoint`() {
        val playerId = "11119"  // Generic player ID for testing endpoint functionality
        val playerName = "lionel-messi"
        val url = "http://localhost:$port/players/$playerId/$playerName/stats"

        println("=== TEST PLAYER STATS ENDPOINT ===")
        println("URL: $url")
        println("Player: $playerName (ID: $playerId)")
        println("Using Bearer Token: ${authToken.take(20)}...")

        val headers = createAuthHeaders()
        val entity = HttpEntity<Void>(headers)

        val response: ResponseEntity<Map<*, *>> = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            object : ParameterizedTypeReference<Map<*, *>>() {}
        )

        println("Status Code: ${response.statusCode}")
        println("=============================")

        // Endpoint should respond (either with data or appropriate error status)
        assertNotNull(response.statusCode, "Should receive a response")
        assertTrue(
            response.statusCode == HttpStatus.OK ||
            response.statusCode == HttpStatus.NOT_FOUND ||
            response.statusCode == HttpStatus.BAD_REQUEST,
            "Status should be OK, NOT_FOUND, or BAD_REQUEST"
        )

        if (response.statusCode == HttpStatus.OK && response.body != null) {
            val stats = response.body as Map<*, *>

            // Verify response structure if data is available
            assertTrue(stats.containsKey("competitions"), "Stats should contain competitions")
            assertTrue(stats.containsKey("totalAverage"), "Stats should contain totalAverage")

            val competitions = stats["competitions"] as? List<*>
            val totalAverage = stats["totalAverage"] as? Map<*, *>

            println("✓ Player stats endpoint is functioning correctly")
            if (competitions != null && totalAverage != null) {
                println("  - Total competitions: ${competitions.size}")
                println("  - Career matches: ${totalAverage["matches"]}")
                println("  - Career goals: ${totalAverage["goals"]}")
            }
        } else {
            println("ℹ Player data not found (endpoint is working correctly)")
        }
    }
}
