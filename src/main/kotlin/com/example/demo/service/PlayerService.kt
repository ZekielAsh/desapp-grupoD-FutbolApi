package com.example.demo.service

import com.example.demo.model.football.CompetitionStats
import org.springframework.stereotype.Service

@Service
class PlayerService(private val scrapperService: ScrapperService) {

    fun getPlayerStats(playerId: String, playerName: String): List<CompetitionStats> {
        return scrapperService.getPlayerSummaryStats(playerId, playerName)
    }

}