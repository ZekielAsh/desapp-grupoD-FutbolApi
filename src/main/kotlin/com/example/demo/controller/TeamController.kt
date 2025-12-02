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
@RequestMapping("/teams")
@Tag(name = "Team", description = "Team information endpoints")
class TeamController(
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

    @Operation(summary = "Get team players", description = "Retrieve squad list for a specific team")
    @TeamPlayersSuccessResponse
    @BadRequestTeamResponse
    @UnauthorizedResponses
    @TeamNotFoundResponse
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
        return handleServiceCall { teamService.getPlayers(id) }
    }

    @Operation(summary = "Get next matches", description = "Retrieve upcoming matches for a team")
    @TeamMatchesSuccessResponse
    @BadRequestTeamResponse
    @UnauthorizedResponses
    @TeamNotFoundResponse
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
        return handleServiceCall { teamService.getNextMatchesByTeamName(id) }
    }
}
