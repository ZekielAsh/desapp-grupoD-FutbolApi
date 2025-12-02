package com.example.demo.controller

import com.example.demo.helpers.*
import com.example.demo.model.ErrorResponse
import com.example.demo.service.TeamService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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

    @Operation(summary = "Get team players from WhoScored", description = "Retrieve players and stats for a team")
    @WhoScoredPlayersSuccessResponse
    @BadRequestWhoScoredResponse
    @WhoScoredTeamNotFoundResponse
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
        return handleServiceCall { teamService.getTeamPlayersByName(teamName) }
    }
}
