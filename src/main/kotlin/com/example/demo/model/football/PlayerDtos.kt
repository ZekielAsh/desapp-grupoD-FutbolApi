package com.example.demo.model.football

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Complete response containing player statistics by competition and totals.
 */
data class PlayerStatsResponse(
    val competitions: List<CompetitionStats>,
    val totalAverage: StatsData?
)

/**
 * Stores player statistics in a specific competition.
 * This is what the final list will return.
 */
data class CompetitionStats(
    val competition: String,
    val statistics: StatsData
)

/**
 * Represents the statistics row from the WhoScored summary table.
 * The fields match the table columns.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unmapped JSON fields
data class StatsData(
    val matches: String,
    val minutes: String,
    val goals: String,
    val assists: String,
    val yellowCards: String,
    val redCards: String,
    val shotsPerGame: String,      // Shots per game
    val keyPasses: String,         // Key passes
    val dribbles: String,          // Dribbles
    val mvp: String,               // Man of the Match
    val rating: String
)