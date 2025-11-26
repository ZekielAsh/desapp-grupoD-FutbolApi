package com.example.demo.controller

import com.example.demo.model.TeamPlayersResponse
import com.example.demo.service.ScrapperService
import com.example.demo.service.TeamService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
        ApiResponse(responseCode = "400", description = "Invalid request")
    ])
    @GetMapping("/team-players")
    fun getTeamPlayers(
        @Parameter(description = "Team name", required = true, example = "Real-Madrid")
        @RequestParam teamName: String
    ): ResponseEntity<TeamPlayersResponse> {
        return try {
            val response = teamService.getTeamPlayersByName(teamName)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}
