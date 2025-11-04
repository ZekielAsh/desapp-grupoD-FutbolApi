package com.example.demo.controller

import com.example.demo.model.football.PlayerDto
import com.example.demo.service.TeamService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.collections.emptyList

@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamService: TeamService
) {

    // GET /teams/{id}/players → devuelve la lista de jugadores (squad)
    @GetMapping("/{id}/players")
    fun getPlayers(@PathVariable id: Long): ResponseEntity<List<PlayerDto>> {
        return try {
            val players = teamService.getPlayers(id)
            ResponseEntity.ok(players)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyList())
        }
    }

    @GetMapping("/{id}/next-matches")
    fun getNextMatches(@PathVariable id: Long): ResponseEntity<Any> {
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
