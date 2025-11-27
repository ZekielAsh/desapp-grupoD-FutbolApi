package com.example.demo.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

/**
 * Custom metrics configuration for business-specific monitoring
 *
 * This class provides counters and timers for tracking custom application events.
 * Metrics are automatically exposed to Prometheus via /actuator/prometheus
 */
@Component
class MetricsConfig(private val meterRegistry: MeterRegistry) {

    // Counter for team queries
    val teamQueriesCounter: Counter = Counter.builder("football.api.team.queries")
        .description("Total number of team queries")
        .register(meterRegistry)

    // Counter for player queries
    val playerQueriesCounter: Counter = Counter.builder("football.api.player.queries")
        .description("Total number of player queries")
        .register(meterRegistry)

    // Counter for prediction requests
    val predictionRequestsCounter: Counter = Counter.builder("football.api.prediction.requests")
        .description("Total number of prediction requests")
        .register(meterRegistry)

    // Timer for external API calls
    val externalApiTimer: Timer = Timer.builder("football.api.external.calls")
        .description("Time taken for external API calls")
        .register(meterRegistry)

    // Counter for API errors
    val apiErrorsCounter: Counter = Counter.builder("football.api.errors")
        .description("Total number of API errors")
        .tag("type", "general")
        .register(meterRegistry)

    // Counter for successful scraping operations
    val scrapingSuccessCounter: Counter = Counter.builder("football.api.scraping.success")
        .description("Total number of successful scraping operations")
        .register(meterRegistry)

    // Counter for failed scraping operations
    val scrapingFailureCounter: Counter = Counter.builder("football.api.scraping.failure")
        .description("Total number of failed scraping operations")
        .register(meterRegistry)
}

