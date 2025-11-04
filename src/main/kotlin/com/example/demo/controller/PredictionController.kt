package com.example.demo.controller

import com.example.demo.service.PredictionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/predictions")
class PredictionController(
    private val predictionService: PredictionService
) {

    @GetMapping("/match")
    fun predictMatch(
        @RequestParam homeTeam: String,
        @RequestParam awayTeam: String,
        @RequestParam homeTeamId: Long,
        @RequestParam awayTeamId: Long
    ): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(
                predictionService.predictMatch(
                    homeTeam,
                    awayTeam,
                    homeTeamId,
                    awayTeamId
                )
            )
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Error generating prediction: ${e.message}")
        }
}