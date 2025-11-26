package com.example.demo.controller

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
import kotlin.collections.emptyList

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
        ApiResponse(responseCode = "400", description = "Invalid request",
            content = [Content(mediaType = "application/json",
                examples = [ExampleObject(value = "[]")]
            )])
    ])
    @GetMapping("/{id}/players")
    fun getPlayers(
        @Parameter(description = "Team ID", required = true, example = "86")
        @PathVariable id: Long
    ): ResponseEntity<List<PlayerDto>> {
        return try {
            val players = teamService.getPlayers(id)
            ResponseEntity.ok(players)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyList())
        }
    }

    @Operation(summary = "Get next matches", description = "Retrieve upcoming matches for a team")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Matches retrieved successfully",
            content = [Content(mediaType = "application/json",
                examples = [ExampleObject(value = """{"matches": [{"competitionName": "string", "homeTeam": "string", "awayTeam": "string", "utcDate": "string", "score": {"fullTime": {"home": 0, "away": 0}}}]}""")]
            )]),
        ApiResponse(responseCode = "400", description = "Invalid request",
            content = [Content(mediaType = "application/json",
                examples = [ExampleObject(value = """"Error retrieving next matches: string"""")]
            )])
    ])
    @GetMapping("/{id}/next-matches")
    fun getNextMatches(
        @Parameter(description = "Team ID", required = true, example = "86")
        @PathVariable id: Long
    ): ResponseEntity<Any> {
        if (id <= 0) {
            return ResponseEntity.badRequest().body("Invalid team ID")
        }
        return try {
            val matches = teamService.getNextMatchesByTeamName(id)
            ResponseEntity.ok(matches)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Error retrieving next matches: ${e.message}")
        }
    }
}
