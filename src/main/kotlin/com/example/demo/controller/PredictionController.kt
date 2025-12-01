package com.example.demo.controller

import com.example.demo.model.ErrorResponse
import com.example.demo.service.PredictionService
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
@RequestMapping("/predictions")
@Tag(name = "Prediction", description = "Match prediction endpoints")
class PredictionController(
    private val predictionService: PredictionService
) {

    @Operation(summary = "Predict match result", description = "Generate prediction for a match between two teams")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Prediction generated successfully",
            content = [Content(mediaType = "application/json",
                examples = [ExampleObject(value = """{"homeTeam": "string", "awayTeam": "string", "trendAnalysis": {"avgRating": 0, "avgGoals": 0, "avgAssists": 0, "recentFormScore": 0}, "statComparison": {"homeTeamStats": {"avgRating": 0, "totalGoals": 0, "totalAssists": 0}, "awayTeamStats": {"avgRating": 0, "totalGoals": 0, "totalAssists": 0}}, "probabilities": {"homeWin": 0, "draw": 0, "awayWin": 0}, "homeRecentForm": {"results": ["string"], "goalsFor": 0, "goalsAgainst": 0, "points": 0, "formScore": 0}, "awayRecentForm": {"results": ["string"], "goalsFor": 0, "goalsAgainst": 0, "points": 0, "formScore": 0}}""")]
            )]),
        ApiResponse(responseCode = "400", description = "Bad request - Invalid parameters",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid team names or IDs provided", "status": 400}""")]
            )]),
        ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Unauthorized", "message": "Authentication required. Please provide a valid Bearer token", "status": 401}""")]
            )]),
        ApiResponse(responseCode = "403", description = "Forbidden - Invalid or expired token",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Forbidden", "message": "Invalid or expired token", "status": 403}""")]
            )]),
        ApiResponse(responseCode = "404", description = "Team not found",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Not Found", "message": "One or both teams not found", "status": 404}""")]
            )])
    ])
    @GetMapping("/match")
    fun predictMatch(
        @Parameter(description = "Home team name", required = true, example = "Real Madrid CF")
        @RequestParam homeTeam: String,
        @Parameter(description = "Away team name", required = true, example = "FC Barcelona")
        @RequestParam awayTeam: String,
        @Parameter(description = "Home team ID", required = true, example = "86")
        @RequestParam homeTeamId: Long,
        @Parameter(description = "Away team ID", required = true, example = "81")
        @RequestParam awayTeamId: Long
    ): ResponseEntity<Any> {
        if (homeTeam.isBlank() || awayTeam.isBlank() || homeTeamId <= 0 || awayTeamId <= 0) {
            return ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", "Invalid team names or IDs provided", 400)
            )
        }
        return try {
            val prediction = predictionService.predictMatch(homeTeam, awayTeam, homeTeamId, awayTeamId)
            ResponseEntity.ok(prediction)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse("Not Found", "One or both teams not found", 404)
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", e.message ?: "Invalid request", 400)
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse("Internal Server Error", "Error generating prediction: ${e.message}", 500)
            )
        }
    }
}