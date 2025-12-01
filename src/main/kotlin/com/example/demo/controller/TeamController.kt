package com.example.demo.controller

import com.example.demo.model.ErrorResponse
import com.example.demo.model.football.PlayerDto
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
@RequestMapping("/teams")
@Tag(name = "Team", description = "Team information endpoints")
class TeamController(
    private val teamService: TeamService
) {

    @Operation(summary = "Get team players", description = "Retrieve squad list for a specific team")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Players retrieved successfully",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Array<PlayerDto>::class),
                examples = [ExampleObject(value = """[{"id": 0, "name": "string", "position": "string", "nationality": "string", "dateOfBirth": "string", "shirtNumber": 0}]""")]
            )]),
        ApiResponse(responseCode = "400", description = "Bad request - Invalid team ID",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid team ID provided", "status": 400}""")]
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
                examples = [ExampleObject(value = """{"error": "Not Found", "message": "Team not found", "status": 404}""")]
            )])
    ])
    @GetMapping("/{id}/players")
    fun getPlayers(
        @Parameter(description = "Team ID", required = true, example = "86")
        @PathVariable id: Long
    ): ResponseEntity<Any> {
        if (id <= 0) {
            return ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", "Invalid team ID provided", 400)
            )
        }
        return try {
            val players = teamService.getPlayers(id)
            ResponseEntity.ok(players)
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

    @Operation(summary = "Get next matches", description = "Retrieve upcoming matches for a team")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Matches retrieved successfully",
            content = [Content(mediaType = "application/json",
                examples = [ExampleObject(value = """{"matches": [{"competitionName": "string", "homeTeam": "string", "awayTeam": "string", "utcDate": "string", "score": {"fullTime": {"home": 0, "away": 0}}}]}""")]
            )]),
        ApiResponse(responseCode = "400", description = "Bad request - Invalid team ID",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(value = """{"error": "Bad Request", "message": "Invalid team ID provided", "status": 400}""")]
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
                examples = [ExampleObject(value = """{"error": "Not Found", "message": "Team not found", "status": 404}""")]
            )])
    ])
    @GetMapping("/{id}/next-matches")
    fun getNextMatches(
        @Parameter(description = "Team ID", required = true, example = "86")
        @PathVariable id: Long
    ): ResponseEntity<Any> {
        if (id <= 0) {
            return ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", "Invalid team ID provided", 400)
            )
        }
        return try {
            val matches = teamService.getNextMatchesByTeamName(id)
            ResponseEntity.ok(matches)
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
                ErrorResponse("Internal Server Error", "Error retrieving next matches: ${e.message}", 500)
            )
        }
    }
}
