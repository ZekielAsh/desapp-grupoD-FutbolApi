package com.example.demo.controller

import com.example.demo.model.ErrorResponse
import com.example.demo.model.TeamPlayersResponse
import com.example.demo.service.TeamService
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
@RequestMapping("/")
@Tag(name = "WhoScored", description = "WhoScored data scraping endpoints")
class WhoScoredController(
    private val teamService: TeamService
) {

    @Operation(summary = "Get team players from WhoScored", description = "Retrieve players and stats for a team")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Team players retrieved successfully",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = TeamPlayersResponse::class),
                examples = [ExampleObject(value = """{"team": "string", "players": [{"name": "string", "appearances": "string", "goals": 0, "assists": 0, "rating": 0}]}""")]
            )]),
        ApiResponse(responseCode = "400", description = "Bad request - Invalid team name",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid team name provided", "status": 400}""")]
            )]),
        ApiResponse(responseCode = "404", description = "Team not found",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Not Found", "message": "Team not found", "status": 404}""")]
            )])
    ])
    @GetMapping("/team-players")
    fun getTeamPlayers(
        @Parameter(description = "Team name", required = true, example = "Real-Madrid")
        @RequestParam teamName: String
    ): ResponseEntity<Any> {
        if (teamName.isBlank()) {
            return ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", "Invalid team name provided", 400)
            )
        }
        return try {
            val response = teamService.getTeamPlayersByName(teamName)
            ResponseEntity.ok(response)
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
                ErrorResponse("Internal Server Error", "Error retrieving team players: ${e.message}", 500)
            )
        }
    }
}
