package com.example.demo.controller

import com.example.demo.model.ErrorResponse
import com.example.demo.service.PlayerService
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/players")
@Tag(name = "Player", description = "Player information endpoints")
class PlayerController(
    private val playerService: PlayerService
) {

    @Operation(summary = "Get player statistics", description = "Retrieve detailed statistics for a player")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = [Content(mediaType = "application/json",
                examples = [ExampleObject(value = """{"competitions": [{"competition": "string", "statistics": {"matches": "string", "minutes": "string", "goals": "string", "assists": "string", "yellowCards": "string", "redCards": "string", "shotsPerGame": "string", "keyPasses": "string", "dribbles": "string", "mvp": "string", "rating": "string"}}], "totalAverage": {"matches": "string", "minutes": "string", "goals": "string", "assists": "string", "yellowCards": "string", "redCards": "string", "shotsPerGame": "string", "keyPasses": "string", "dribbles": "string", "mvp": "string", "rating": "string"}}""")]
            )]),
        ApiResponse(responseCode = "400", description = "Bad request - Invalid player data",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid player ID or name provided", "status": 400}""")]
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
        ApiResponse(responseCode = "404", description = "Player not found",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Not Found", "message": "Player not found", "status": 404}""")]
            )])
    ])
    @GetMapping("/{id}/{name}/stats")
    fun getPlayerStats(
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
            val playerStats = playerService.getPlayerStats(id, name)
            ResponseEntity.ok(playerStats)
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
                ErrorResponse("Internal Server Error", "Error retrieving player stats: ${e.message}", 500)
            )
        }
    }
}