package com.example.demo.controller

import com.example.demo.model.ErrorResponse
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
import org.springframework.http.HttpStatus
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
            description = "Bad request - Invalid team ID",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid team ID provided", "status": 400}""")]
            )]),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Unauthorized", "message": "Authentication required. Please provide a valid Bearer token", "status": 401}""")]
            )]),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden - Invalid or expired token",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Forbidden", "message": "Invalid or expired token", "status": 403}""")]
            )]),
        ApiResponse(
            responseCode = "404",
            description = "Team not found",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Not Found", "message": "Team not found", "status": 404}""")]
            )])
    ])
    @GetMapping("/teams/{id}")
    fun getTeamMetrics(
        @Parameter(description = "Team ID", required = true, example = "86")
        @PathVariable id: Long
    ): ResponseEntity<Any> {
        if (id <= 0) {
            return ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", "Invalid team ID provided", 400)
            )
        }
        return try {
            val metrics = advancedMetricsService.getTeamAdvancedMetrics(id)
            ResponseEntity.ok(metrics)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse("Not Found", "Team not found", 404)
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", e.message ?: "Invalid request", 400)
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse("Internal Server Error", "Error retrieving team metrics: ${e.message}", 500)
            )
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
            description = "Bad request - Invalid player data",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid player ID or name provided", "status": 400}""")]
            )]),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Unauthorized", "message": "Authentication required. Please provide a valid Bearer token", "status": 401}""")]
            )]),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden - Invalid or expired token",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Forbidden", "message": "Invalid or expired token", "status": 403}""")]
            )]),
        ApiResponse(
            responseCode = "404",
            description = "Player not found",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Not Found", "message": "Player not found", "status": 404}""")]
            )])
    ])
    @GetMapping("/players/{id}/{name}")
    fun getPlayerMetrics(
        @Parameter(description = "Player ID", required = true, example = "44")
        @PathVariable id: String,
        @Parameter(description = "Player name", required = true, example = "Lionel-Messi")
        @PathVariable name: String
    ): ResponseEntity<Any> {
        if (id.isBlank() || name.isBlank()) {
            return ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", "Invalid player ID or name provided", 400)
            )
        }
        return try {
            val metrics = advancedMetricsService.getPlayerAdvancedMetrics(id, name)
            ResponseEntity.ok(metrics)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse("Not Found", "Player not found", 404)
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", e.message ?: "Invalid request", 400)
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse("Internal Server Error", "Error retrieving player metrics: ${e.message}", 500)
            )
        }
    }
}

