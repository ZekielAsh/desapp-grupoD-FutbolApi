package com.example.demo.e2e

import com.example.demo.model.football.MatchDto
import com.example.demo.model.football.PlayerDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@ImportAutoConfiguration(
    exclude = [
        com.example.demo.security.SecurityConfig::class
    ]
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TeamControllerE2eTest {


    @LocalServerPort
    var port: Int = 0


    @Autowired
    lateinit var restTemplate: TestRestTemplate


    @Test
    fun `getPlayers returns list`() {
        val teamId = 65L
        val url = "http://localhost:$port/teams/$teamId/players"


        val response: ResponseEntity<List<PlayerDto>> = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<PlayerDto>>() {}
        )


        assertTrue(response.statusCode == HttpStatus.OK)
        assertNotNull(response.body)
    }


    @Test
    fun `getNextMatches returns list`() {
        val teamId = 65L
        val url = "http://localhost:$port/teams/$teamId/next-matches"


        val response: ResponseEntity<List<MatchDto>> = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<MatchDto>>() {}
        )


        assertTrue(response.statusCode == HttpStatus.OK)
        assertNotNull(response.body)
    }
}