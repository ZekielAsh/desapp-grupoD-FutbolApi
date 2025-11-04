package com.example.demo.service

import com.example.demo.model.MatchPredictionResponse
import com.example.demo.model.RecentForm
import com.example.demo.model.StatComparison
import com.example.demo.model.TeamAggregateStats
import com.example.demo.model.TeamPlayersResponse
import com.example.demo.model.TrendAnalysis
import com.example.demo.model.WinProbabilities
import org.springframework.stereotype.Service

@Service
class PredictionService(
    private val scrapperService: ScrapperService,
    private val teamService: TeamService
) {

    fun predictMatch(homeTeam: String, awayTeam: String): MatchPredictionResponse {

        // ✅ Obtener estadísticas reales de WhoScored (scrapping)
        val homeData = scrapperService.getTeamPlayersByName(homeTeam)
        val awayData = scrapperService.getTeamPlayersByName(awayTeam)

        // ✅ Agregar estadísticas agregadas basadas en los jugadores
        val homeStats = aggregateStats(homeData)
        val awayStats = aggregateStats(awayData)

        // ✅ Tendencias basadas SOLO en info disponible (no partidos previos)
        val trend = TrendAnalysis(
            avgRating = (homeStats.avgRating + awayStats.avgRating) / 2,
            avgGoals = (homeStats.totalGoals + awayStats.totalGoals) / 2.0,
            avgAssists = (homeStats.totalAssists + awayStats.totalAssists) / 2.0,
            recentFormScore = 0.0 // ❗ No se puede calcular con tus DTO
        )

        val statComparison = StatComparison(homeStats, awayStats)

        // ✅ Predicción basada SOLO en estadísticas disponibles
        val probabilities = computeProbabilities(homeStats, awayStats)

        return MatchPredictionResponse(
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            trendAnalysis = trend,
            statComparison = statComparison,
            probabilities = probabilities
        )
    }

    private fun aggregateStats(team: TeamPlayersResponse): TeamAggregateStats {
        val players = team.players

        val avgRating = players.map { it.rating }.average()
        val totalGoals = players.sumOf { it.goals }
        val totalAssists = players.sumOf { it.assists }

        return TeamAggregateStats(
            avgRating = avgRating,
            totalGoals = totalGoals,
            totalAssists = totalAssists
        )
    }

    private fun computeProbabilities(
        home: TeamAggregateStats,
        away: TeamAggregateStats
    ): WinProbabilities {

        val ratingDiff = home.avgRating - away.avgRating
        val goalsDiff = home.totalGoals - away.totalGoals

        var homeP = 0.40 + ratingDiff * 0.04 + goalsDiff * 0.01
        var awayP = 0.40 - ratingDiff * 0.04 - goalsDiff * 0.01
        var drawP = 0.20

        // Normalización
        val sum = homeP + drawP + awayP

        return WinProbabilities(
            homeWin = homeP / sum,
            draw = drawP / sum,
            awayWin = awayP / sum
        )
    }
}