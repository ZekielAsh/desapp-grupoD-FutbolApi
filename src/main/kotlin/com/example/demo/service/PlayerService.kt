package com.example.demo.service

import com.example.demo.model.football.PlayerStatsResponse
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PlayerService(private val scrapperService: ScrapperService) {

    @Cacheable("playerStats", key = "#playerId + '_' + #playerName")
    fun getPlayerStats(playerId: String, playerName: String): PlayerStatsResponse {
        return scrapperService.getPlayerSummaryStats(playerId, playerName)
    }

}