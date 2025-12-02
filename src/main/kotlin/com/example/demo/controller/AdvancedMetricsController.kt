package com.example.demo.controller

import com.example.demo.helpers.BadRequestPlayerResponse
import com.example.demo.helpers.BadRequestTeamResponse
import com.example.demo.helpers.PlayerMetricsSuccessResponse
import com.example.demo.helpers.PlayerNotFoundResponse
import com.example.demo.helpers.TeamMetricsSuccessResponse
import com.example.demo.helpers.TeamNotFoundResponse
import com.example.demo.helpers.UnauthorizedResponses
import com.example.demo.model.ErrorResponse
import com.example.demo.service.AdvancedMetricsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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

    @Operation(
        summary = "Get team advanced metrics",
        description = "Retrieve comprehensive statistical analysis for a team including performance, form, and strength indicators"
    )
    @TeamMetricsSuccessResponse
    @BadRequestTeamResponse
    @UnauthorizedResponses
    @TeamNotFoundResponse
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
        return handleServiceCall { advancedMetricsService.getTeamAdvancedMetrics(id) }
    }

    @Operation(
        summary = "Get player advanced metrics",
        description = "Retrieve comprehensive statistical analysis for a player including per-90 stats, efficiency metrics, and calculated performance indicators"
    )
    @PlayerMetricsSuccessResponse
    @BadRequestPlayerResponse
    @UnauthorizedResponses
    @PlayerNotFoundResponse
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
        return handleServiceCall { advancedMetricsService.getPlayerAdvancedMetrics(id, name) }
    }
}

