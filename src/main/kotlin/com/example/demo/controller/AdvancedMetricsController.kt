package com.example.demo.controller

import com.example.demo.model.PlayerAdvancedMetrics
import com.example.demo.model.TeamAdvancedMetrics
import com.example.demo.service.AdvancedMetricsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/metrics")
@Tag(name = "Advanced Metrics", description = "Advanced statistical metrics endpoints")
class AdvancedMetricsController(
    private val advancedMetricsService: AdvancedMetricsService
) {

    @Operation(
        summary = "Get team advanced metrics",
        description = "Retrieve comprehensive statistical analysis for a team including performance, form, and strength indicators"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Metrics retrieved successfully",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = TeamAdvancedMetrics::class),
                examples = [ExampleObject(value = """{
  "teamId": 0,
  "teamName": "string",
  "season": "string",
  "averageGoalsScored": 0.0,
  "averageGoalsConceded": 0.0,
  "cleanSheets": 0,
  "winRate": 0.0,
  "drawRate": 0.0,
  "lossRate": 0.0,
  "goalsPerMatch": 0.0,
  "goalsConcededPerMatch": 0.0,
  "goalDifference": 0,
  "homePerformance": {
    "played": 0,
    "wins": 0,
    "draws": 0,
    "losses": 0,
    "goalsFor": 0,
    "goalsAgainst": 0,
    "points": 0,
    "winRate": 0.0
  },
  "awayPerformance": {
    "played": 0,
    "wins": 0,
    "draws": 0,
    "losses": 0,
    "goalsFor": 0,
    "goalsAgainst": 0,
    "points": 0,
    "winRate": 0.0
  },
  "recentForm": {
    "last5Matches": ["string"],
    "points": 0,
    "goalsScored": 0,
    "goalsConceded": 0,
    "formScore": 0.0
  },
  "attackStrength": 0.0,
  "defenseStrength": 0.0
}""")]
            )]),
        ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(value = """"Error retrieving team metrics: string"""")]
            )])
    ])
    @GetMapping("/teams/{id}")
    fun getTeamMetrics(
        @Parameter(description = "Team ID", required = true, example = "86")
        @PathVariable id: Long
    ): ResponseEntity<Any> {
        return try {
            val metrics = advancedMetricsService.getTeamAdvancedMetrics(id)
            ResponseEntity.ok(metrics)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Error retrieving team metrics: ${e.message}")
        }
    }

    @Operation(
        summary = "Get player advanced metrics",
        description = "Retrieve comprehensive statistical analysis for a player including per-90 stats, efficiency metrics, and calculated performance indicators"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Metrics retrieved successfully",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = PlayerAdvancedMetrics::class),
                examples = [ExampleObject(value = """{
  "playerId": "string",
  "playerName": "string",
  "season": "string",
  "totalMatches": 0,
  "totalMinutes": 0,
  "goalsPerMatch": 0.0,
  "assistsPerMatch": 0.0,
  "goalsPer90": 0.0,
  "assistsPer90": 0.0,
  "goalContribution": 0,
  "goalContributionPer90": 0.0,
  "averageRating": 0.0,
  "keyPassesPer90": 0.0,
  "dribblesPer90": 0.0,
  "shotsPerGame": 0.0,
  "minutesPerGoal": 0.0,
  "minutesPerAssist": 0.0,
  "discipline": {
    "yellowCards": 0,
    "redCards": 0,
    "yellowCardsPerMatch": 0.0,
    "redCardsPerMatch": 0.0
  },
  "efficiency": {
    "shotAccuracy": 0.0,
    "creativeEfficiency": 0.0,
    "dribbleSuccessRate": 0.0,
    "playingTimePercentage": 0.0
  },
  "impactScore": 0.0,
  "versatilityIndex": 0.0
}""")]
            )]),
        ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(value = """"Error retrieving player metrics: string"""")]
            )])
    ])
    @GetMapping("/players/{id}/{name}")
    fun getPlayerMetrics(
        @Parameter(description = "Player ID", required = true, example = "44")
        @PathVariable id: String,
        @Parameter(description = "Player name", required = true, example = "Lionel-Messi")
        @PathVariable name: String
    ): ResponseEntity<Any> {
        return try {
            val metrics = advancedMetricsService.getPlayerAdvancedMetrics(id, name)
            ResponseEntity.ok(metrics)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Error retrieving player metrics: ${e.message}")
        }
    }
}

