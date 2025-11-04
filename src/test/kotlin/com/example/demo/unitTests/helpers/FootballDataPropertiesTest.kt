package com.example.demo.unitTests.helpers

import com.example.demo.helpers.FootballDataProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class FootballDataPropertiesTest {

    @Test
    fun `test FootballDataProperties creation with all fields`() {
        val baseUrl = "https://api.football-data.org/v4"
        val apiKey = "test-api-key-12345"

        val properties = FootballDataProperties(
            baseUrl = baseUrl,
            apiKey = apiKey
        )

        assertEquals(baseUrl, properties.baseUrl)
        assertEquals(apiKey, properties.apiKey)
    }

    @Test
    fun `test FootballDataProperties with different URLs`() {
        val prodUrl = "https://api.football-data.org/v4"
        val devUrl = "https://dev-api.football-data.org/v4"

        val prodProperties = FootballDataProperties(prodUrl, "prod-key")
        val devProperties = FootballDataProperties(devUrl, "dev-key")

        assertEquals(prodUrl, prodProperties.baseUrl)
        assertEquals(devUrl, devProperties.baseUrl)
        assertNotEquals(prodProperties.baseUrl, devProperties.baseUrl)
    }

    @Test
    fun `test FootballDataProperties copy functionality`() {
        val original = FootballDataProperties(
            baseUrl = "https://api.example.com",
            apiKey = "original-key"
        )

        val updated = original.copy(apiKey = "new-key")

        assertEquals(original.baseUrl, updated.baseUrl)
        assertEquals("new-key", updated.apiKey)
        assertNotEquals(original.apiKey, updated.apiKey)
    }

    @Test
    fun `test FootballDataProperties with empty strings`() {
        val properties = FootballDataProperties(
            baseUrl = "",
            apiKey = ""
        )

        assertEquals("", properties.baseUrl)
        assertEquals("", properties.apiKey)
    }

    @Test
    fun `test FootballDataProperties equals and hashCode`() {
        val props1 = FootballDataProperties("https://api.test.com", "key123")
        val props2 = FootballDataProperties("https://api.test.com", "key123")
        val props3 = FootballDataProperties("https://api.other.com", "key123")

        assertEquals(props1, props2)
        assertEquals(props1.hashCode(), props2.hashCode())
        assertNotEquals(props1, props3)
    }
}

