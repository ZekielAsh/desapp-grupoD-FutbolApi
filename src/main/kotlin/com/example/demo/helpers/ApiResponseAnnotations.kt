package com.example.demo.helpers

import com.example.demo.model.ApiAuditLog
import com.example.demo.model.ErrorResponse
import com.example.demo.model.PlayerAdvancedMetrics
import com.example.demo.model.TeamAdvancedMetrics
import com.example.demo.model.TeamComparisonResponse
import com.example.demo.model.football.PlayerDto
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

/**
 * Common API responses for unauthorized access (401 & 403)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication required",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Unauthorized", "message": "Authentication required. Please provide a valid Bearer token", "status": 401}""")]
            )]
        ),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden - Invalid or expired token",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Forbidden", "message": "Invalid or expired token", "status": 403}""")]
            )]
        )
    ]
)
annotation class UnauthorizedResponses

// ========== TEAM ENDPOINTS ==========

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "Bad request - Invalid team ID",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = ErrorResponse::class),
        examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid team ID provided", "status": 400}""")]
    )]
)
annotation class BadRequestTeamResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "Team not found",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = ErrorResponse::class),
        examples = [ExampleObject(value = """{"error": "Not Found", "message": "Team not found", "status": 404}""")]
    )]
)
annotation class TeamNotFoundResponse

// ========== PLAYER ENDPOINTS ==========

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "Bad request - Invalid player data",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = ErrorResponse::class),
        examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid player ID or name provided", "status": 400}""")]
    )]
)
annotation class BadRequestPlayerResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "Player not found",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = ErrorResponse::class),
        examples = [ExampleObject(value = """{"error": "Not Found", "message": "Player not found", "status": 404}""")]
    )]
)
annotation class PlayerNotFoundResponse

// ========== ADVANCED METRICS ENDPOINTS ==========

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
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
    )]
)
annotation class TeamMetricsSuccessResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
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
    )]
)
annotation class PlayerMetricsSuccessResponse

// ========== TEAM CONTROLLER ==========

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "200",
    description = "Players retrieved successfully",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = Array<PlayerDto>::class),
        examples = [ExampleObject(value = """[{"id": 0, "name": "string", "position": "string", "nationality": "string", "dateOfBirth": "string", "shirtNumber": 0}]""")]
    )]
)
annotation class TeamPlayersSuccessResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "200",
    description = "Matches retrieved successfully",
    content = [Content(
        mediaType = "application/json",
        examples = [ExampleObject(value = """{"matches": [{"competitionName": "string", "homeTeam": "string", "awayTeam": "string", "utcDate": "string", "score": {"fullTime": {"home": 0, "away": 0}}}]}""")]
    )]
)
annotation class TeamMatchesSuccessResponse

// ========== PLAYER CONTROLLER ==========

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "200",
    description = "Statistics retrieved successfully",
    content = [Content(
        mediaType = "application/json",
        examples = [ExampleObject(value = """{"competitions": [{"competition": "string", "statistics": {"matches": "string", "minutes": "string", "goals": "string", "assists": "string", "yellowCards": "string", "redCards": "string", "shotsPerGame": "string", "keyPasses": "string", "dribbles": "string", "mvp": "string", "rating": "string"}}], "totalAverage": {"matches": "string", "minutes": "string", "goals": "string", "assists": "string", "yellowCards": "string", "redCards": "string", "shotsPerGame": "string", "keyPasses": "string", "dribbles": "string", "mvp": "string", "rating": "string"}}""")]
    )]
)
annotation class PlayerStatsSuccessResponse

// ========== PREDICTION CONTROLLER ==========

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "200",
    description = "Prediction generated successfully",
    content = [Content(
        mediaType = "application/json",
        examples = [ExampleObject(value = """{"homeTeam": "string", "awayTeam": "string", "trendAnalysis": {"avgRating": 0, "avgGoals": 0, "avgAssists": 0, "recentFormScore": 0}, "statComparison": {"homeTeamStats": {"avgRating": 0, "totalGoals": 0, "totalAssists": 0}, "awayTeamStats": {"avgRating": 0, "totalGoals": 0, "totalAssists": 0}}, "probabilities": {"homeWin": 0, "draw": 0, "awayWin": 0}, "homeRecentForm": {"results": ["string"], "goalsFor": 0, "goalsAgainst": 0, "points": 0, "formScore": 0}, "awayRecentForm": {"results": ["string"], "goalsFor": 0, "goalsAgainst": 0, "points": 0, "formScore": 0}}""")]
    )]
)
annotation class PredictionSuccessResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "Bad request - Invalid parameters",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = ErrorResponse::class),
        examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid team names or IDs provided", "status": 400}""")]
    )]
)
annotation class BadRequestPredictionResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "Team not found",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = ErrorResponse::class),
        examples = [ExampleObject(value = """{"error": "Not Found", "message": "One or both teams not found", "status": 404}""")]
    )]
)
annotation class TeamsNotFoundResponse

// ========== TEAM COMPARISON CONTROLLER ==========

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "200",
    description = "Teams compared successfully",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = TeamComparisonResponse::class),
        examples = [ExampleObject(value = """{
  "team1": {
    "id": 0,
    "name": "string",
    "wins": 0,
    "draws": 0,
    "losses": 0,
    "goalsFor": 0,
    "goalsAgainst": 0,
    "points": 0,
    "position": 0,
    "form": "string"
  },
  "team2": {
    "id": 0,
    "name": "string",
    "wins": 0,
    "draws": 0,
    "losses": 0,
    "goalsFor": 0,
    "goalsAgainst": 0,
    "points": 0,
    "position": 0,
    "form": "string"
  },
  "headToHead": {
    "team1Wins": 0,
    "team2Wins": 0,
    "draws": 0,
    "totalMatches": 0,
    "lastMeetings": [
      {
        "date": "string",
        "homeTeam": "string",
        "awayTeam": "string",
        "homeScore": 0,
        "awayScore": 0,
        "winner": "string"
      }
    ]
  }
}""")]
    )]
)
annotation class TeamComparisonSuccessResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "Bad request - Invalid team IDs",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = ErrorResponse::class),
        examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid team IDs provided", "status": 400}""")]
    )]
)
annotation class BadRequestTeamComparisonResponse

// ========== API AUDIT CONTROLLER ==========

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "200",
    description = "Audit logs retrieved successfully",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = Array<ApiAuditLog>::class),
        examples = [ExampleObject(value = """[{"id": 0, "httpMethod": "string", "path": "string", "controllerName": "string", "methodName": "string", "params": "string", "executionTimeMs": 0, "wasSuccess": true, "errorMessage": "string", "timestamp": "string"}]""")]
    )]
)
annotation class AuditLogsSuccessResponse

// ========== WHOSCORED CONTROLLER ==========

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "200",
    description = "Team players retrieved successfully",
    content = [Content(
        mediaType = "application/json",
        examples = [ExampleObject(value = """{"team": "string", "players": [{"name": "string", "appearances": "string", "goals": 0, "assists": 0, "rating": 0}]}""")]
    )]
)
annotation class WhoScoredPlayersSuccessResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "Bad request - Invalid team name",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = ErrorResponse::class),
        examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid team name provided", "status": 400}""")]
    )]
)
annotation class BadRequestWhoScoredResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "Team not found",
    content = [Content(
        mediaType = "application/json",
        schema = Schema(implementation = ErrorResponse::class),
        examples = [ExampleObject(value = """{"error": "Not Found", "message": "Team not found", "status": 404}""")]
    )]
)
annotation class WhoScoredTeamNotFoundResponse
