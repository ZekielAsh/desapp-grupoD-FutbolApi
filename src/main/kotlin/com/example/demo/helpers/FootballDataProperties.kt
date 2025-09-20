package com.example.demo.helpers

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "football-data")
data class FootballDataProperties(
    val baseUrl: String,
    val apiKey: String
)
