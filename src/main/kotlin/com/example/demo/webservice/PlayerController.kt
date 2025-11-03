package com.example.demo.webservice

import com.example.demo.service.PlayerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/players")
class PlayerController(
    private val playerService: PlayerService
) {

    @GetMapping("/{id}/{name}/stats")
    fun getPlayerStats(@PathVariable id: String, @PathVariable name: String): ResponseEntity<Any> {
        try {
            val playerStats = playerService.getPlayerStats(id, name)
            return ResponseEntity.ok(playerStats)
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("Error retrieving player stats: ${e.message}")
        }
    }
}