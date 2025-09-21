package com.example.demo.controller

import com.example.demo.model.TeamPlayersResponse
import com.example.demo.service.WhoScoredScrapingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/")
class WhoScoredController(
    private val whoScoredScrapingService: WhoScoredScrapingService
) {

    @GetMapping("/team-players")
    fun getTeamPlayers(@RequestParam teamName: String): ResponseEntity<TeamPlayersResponse> {
        return try {
            val response = whoScoredScrapingService.getTeamPlayersByName(teamName)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}
