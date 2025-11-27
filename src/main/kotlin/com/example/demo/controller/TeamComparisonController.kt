package com.example.demo.controller

import com.example.demo.model.TeamComparisonResponse
import com.example.demo.service.TeamComparisonService
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
@RequestMapping("/teams")
@Tag(name = "Team Comparison", description = "Team comparison endpoints")
class TeamComparisonController(
    private val teamComparisonService: TeamComparisonService
) {

    @Operation(
        summary = "Compare two teams",
        description = "Get comprehensive comparison metrics between two teams including stats, form, and head-to-head record"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Teams compared successfully",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = TeamComparisonResponse::class),
                examples = [ExampleObject(value = """{
  "team1": {
    "id": 0,
    "name": "string",
    "wins": 0,
    "draws": 0,
    "losses": 0,
    "goalsFor": 0,
    "goalsAgainst": 0,
    "points": 0,
    "position": 0,
    "form": "string"
  },
  "team2": {
    "id": 0,
    "name": "string",
    "wins": 0,
    "draws": 0,
    "losses": 0,
    "goalsFor": 0,
    "goalsAgainst": 0,
    "points": 0,
    "position": 0,
    "form": "string"
  },
  "headToHead": {
    "team1Wins": 0,
    "team2Wins": 0,
    "draws": 0,
    "totalMatches": 0,
    "lastMeetings": [
      {
        "date": "string",
        "homeTeam": "string",
        "awayTeam": "string",
        "homeScore": 0,
        "awayScore": 0,
        "winner": "string"
      }
    ]
  }
}""")]
            )]),
        ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(value = """"Error comparing teams: string"""")]
            )])
    ])
    @GetMapping("/compare")
    fun compareTeams(
        @Parameter(description = "First team ID", required = true, example = "86")
        @RequestParam team1: Long,
        @Parameter(description = "Second team ID", required = true, example = "81")
        @RequestParam team2: Long
    ): ResponseEntity<Any> {
        return try {
            val comparison = teamComparisonService.compareTeams(team1, team2)
            ResponseEntity.ok(comparison)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Error comparing teams: ${e.message}")
        }
    }
}

