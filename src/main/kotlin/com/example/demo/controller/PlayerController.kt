package com.example.demo.controller

import com.example.demo.helpers.*
import com.example.demo.model.ErrorResponse
import com.example.demo.service.PlayerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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

    @Operation(summary = "Get player statistics", description = "Retrieve detailed statistics for a player")
    @PlayerStatsSuccessResponse
    @BadRequestPlayerResponse
    @UnauthorizedResponses
    @PlayerNotFoundResponse
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
        return handleServiceCall { playerService.getPlayerStats(id, name) }
    }
}