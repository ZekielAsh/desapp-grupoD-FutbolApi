package com.example.demo.controller

import com.example.demo.helpers.*
import com.example.demo.model.ErrorResponse
import com.example.demo.service.TeamComparisonService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/teams")
@Tag(name = "Team Comparison", description = "Team comparison endpoints")
class TeamComparisonController(
    private val teamComparisonService: TeamComparisonService
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
        summary = "Compare two teams",
        description = "Get comprehensive comparison metrics between two teams including stats, form, and head-to-head record"
    )
    @TeamComparisonSuccessResponse
    @BadRequestTeamComparisonResponse
    @UnauthorizedResponses
    @TeamsNotFoundResponse
    @GetMapping("/compare")
    fun compareTeams(
        @Parameter(description = "First team ID", required = true, example = "86")
        @RequestParam team1: Long,
        @Parameter(description = "Second team ID", required = true, example = "81")
        @RequestParam team2: Long
    ): ResponseEntity<Any> {
        if (team1 <= 0 || team2 <= 0) {
            return ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", "Invalid team IDs provided", 400)
            )
        }
        if (team1 == team2) {
            return ResponseEntity.badRequest().body(
                ErrorResponse("Bad Request", "Cannot compare the same team", 400)
            )
        }
        return handleServiceCall { teamComparisonService.compareTeams(team1, team2) }
    }
}
