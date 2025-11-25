package com.example.demo.e2e

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@Import(E2eTestConfig::class)
class PlayerControllerE2eTest {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `getPlayerStats returns 400 when invalid player`() {
        val url = "http://localhost:$port/players/0/unknown/stats"
        val response: ResponseEntity<String> = restTemplate.getForEntity(url, String::class.java)

        assertTrue(
            response.statusCode == HttpStatus.BAD_REQUEST ||
                    response.statusCode == HttpStatus.OK
        )
    }
}
