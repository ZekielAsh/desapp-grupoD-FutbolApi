package com.example.demo.helpers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.mock
import org.springframework.web.client.RestClient

class FootballDataClientTest {

    @Test
    fun `test footballRestClient bean creation`() {
        val properties = FootballDataProperties(
            baseUrl = "https://api.football-data.org/v4",
            apiKey = "test-key-123"
        )
        val client = FootballDataClient(properties)

        val restClient = client.footballRestClient()

        assertNotNull(restClient)
    }

    @Test
    fun `test footballRestClient with different properties`() {
        val props1 = FootballDataProperties("https://api1.com", "key1")
        val props2 = FootballDataProperties("https://api2.com", "key2")

        val client1 = FootballDataClient(props1)
        val client2 = FootballDataClient(props2)

        val restClient1 = client1.footballRestClient()
        val restClient2 = client2.footballRestClient()

        assertNotNull(restClient1)
        assertNotNull(restClient2)
        assertNotSame(restClient1, restClient2)
    }

    @Test
    fun `test footballRestClient bean is created correctly`() {
        val baseUrl = "https://test-api.com/v4"
        val apiKey = "my-secret-key"
        val properties = FootballDataProperties(baseUrl, apiKey)
        val client = FootballDataClient(properties)

        val restClient = client.footballRestClient()

        assertNotNull(restClient)
        // The RestClient should be configured with the base URL and header
        // We can't directly test these without making actual HTTP calls
        // but we can verify it's created without errors
    }
}

