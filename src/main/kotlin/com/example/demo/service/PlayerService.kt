package com.example.demo.service

import com.example.demo.model.football.PlayerStatsResponse
import org.springframework.stereotype.Service

@Service
class PlayerService(private val scrapperService: ScrapperService) {

    fun getPlayerStats(playerId: String, playerName: String): PlayerStatsResponse {
        return scrapperService.getPlayerSummaryStats(playerId, playerName)
    }

}