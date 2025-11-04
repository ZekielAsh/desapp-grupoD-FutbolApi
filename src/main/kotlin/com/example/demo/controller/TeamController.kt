package com.example.demo.controller

import com.example.demo.model.football.PlayerDto
import com.example.demo.service.TeamService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamService: TeamService
) {

    // GET /teams/{id}/players → devuelve la lista de jugadores (squad)
    @GetMapping("/{id}/players")
    fun getPlayers(@PathVariable id: Long): ResponseEntity<List<PlayerDto>> {
        val players = teamService.getPlayers(id)
        return ResponseEntity.ok(players)
    }

    @GetMapping("/{id}/next-matches")
    fun getNextMatches(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            val matches = teamService.getNextMatchesByTeamName(id)
            ResponseEntity.ok(matches)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Error retrieving next matches: ${e.message}")
        }
    }
}
