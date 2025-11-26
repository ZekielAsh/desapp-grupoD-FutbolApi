package com.example.demo.controller

import com.example.demo.service.PredictionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
        ApiResponse(responseCode = "400", description = "Invalid request",
            content = [Content(mediaType = "application/json",
                examples = [ExampleObject(value = """"Error generating prediction: string"""")]
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
    ): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(
                predictionService.predictMatch(
                    homeTeam,
                    awayTeam,
                    homeTeamId,
                    awayTeamId
                )
            )
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Error generating prediction: ${e.message}")
        }
}