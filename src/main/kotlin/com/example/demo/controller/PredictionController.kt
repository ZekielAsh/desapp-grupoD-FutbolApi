package com.example.demo.controller

import com.example.demo.helpers.*
import com.example.demo.model.ErrorResponse
import com.example.demo.service.PredictionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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

    private fun <T> handleServiceCall(operation: () -> T): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(operation())
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse("Not Found", e.message ?: "Resource not found", 404)
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", e.message ?: "Invalid request", 400)
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse("Internal Server Error", "Error processing request: ${e.message}", 500)
            )
        }
    }

    @Operation(summary = "Predict match result", description = "Generate prediction for a match between two teams")
    @PredictionSuccessResponse
    @BadRequestPredictionResponse
    @UnauthorizedResponses
    @TeamsNotFoundResponse
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
        return handleServiceCall { predictionService.predictMatch(homeTeam, awayTeam, homeTeamId, awayTeamId) }
    }
}