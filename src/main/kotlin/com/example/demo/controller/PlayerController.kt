package com.example.demo.controller

import com.example.demo.service.PlayerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
        ApiResponse(responseCode = "400", description = "Invalid request",
            content = [Content(mediaType = "application/json",
                examples = [ExampleObject(value = """"Error retrieving player stats: string"""")]
            )])
    ])
    @GetMapping("/{id}/{name}/stats")
    fun getPlayerStats(
        @Parameter(description = "Player ID", required = true, example = "44")
        @PathVariable id: String,
        @Parameter(description = "Player name", required = true, example = "Lionel-Messi")
        @PathVariable name: String
    ): ResponseEntity<Any> {
        try {
            val playerStats = playerService.getPlayerStats(id, name)
            return ResponseEntity.ok(playerStats)
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("Error retrieving player stats: ${e.message}")
        }
    }
}