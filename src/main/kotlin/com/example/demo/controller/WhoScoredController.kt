package com.example.demo.controller

import com.example.demo.model.TeamPlayersResponse
import com.example.demo.service.ScrapperService
import com.example.demo.service.TeamService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/")
class WhoScoredController(
    private val teamService: TeamService
) {

    @GetMapping("/team-players")
    fun getTeamPlayers(@RequestParam teamName: String): ResponseEntity<TeamPlayersResponse> {
        return try {
            val response = teamService.getTeamPlayersByName(teamName)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}
