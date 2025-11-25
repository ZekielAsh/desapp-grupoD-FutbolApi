package com.example.demo.service

import com.example.demo.model.prediction.MatchPredictionResponse
import com.example.demo.model.prediction.StatComparison
import com.example.demo.model.prediction.TeamAggregateStats
import com.example.demo.model.TeamPlayersResponse
import com.example.demo.model.prediction.RecentForm
import com.example.demo.model.prediction.TrendAnalysis
import com.example.demo.model.prediction.WinProbabilities
import com.google.common.annotations.VisibleForTesting
import org.springframework.stereotype.Service
import kotlin.times

@Service
class PredictionService(
    private val scrapperService: ScrapperService,
    private val teamService: TeamService
) {

    fun predictMatch(homeTeam: String, awayTeam: String, homeTeamId: Long, awayTeamId: Long): MatchPredictionResponse {

        val homeData = scrapperService.getTeamPlayersByName(homeTeam)
        val awayData = scrapperService.getTeamPlayersByName(awayTeam)

        val homeStats = aggregateStats(homeData)
        val awayStats = aggregateStats(awayData)

        val homeForm = computeRecentForm(homeTeamId)
        val awayForm = computeRecentForm(awayTeamId)

        val trend = TrendAnalysis(
            avgRating = (homeStats.avgRating + awayStats.avgRating) / 2,
            avgGoals = (homeStats.totalGoals + awayStats.totalGoals) / 2.0,
            avgAssists = (homeStats.totalAssists + awayStats.totalAssists) / 2.0,
            recentFormScore = (homeForm.formScore + awayForm.formScore) / 2.0
        )

        val comparison = StatComparison(homeStats, awayStats)

        val probabilities = computeProbabilities(homeStats, awayStats, homeForm, awayForm)

        return MatchPredictionResponse(
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            trendAnalysis = trend,
            statComparison = comparison,
            probabilities = probabilities,
            homeRecentForm = homeForm,
            awayRecentForm = awayForm
        )
    }

    private fun aggregateStats(team: TeamPlayersResponse): TeamAggregateStats {
        val players = team.players

        val avgRating = players.map { it.rating }.average()
        val totalGoals = players.sumOf { it.goals }
        val totalAssists = players.sumOf { it.assists }

        return TeamAggregateStats(avgRating, totalGoals, totalAssists)
    }

    private fun computeRecentForm(teamId: Long, teamName: String? = null): RecentForm {
        val matches = teamService.getLastFinishedMatches(teamId)

        if (matches.isEmpty()) return RecentForm(emptyList(), 0, 0, 0, 0.0)

        var gf = 0
        var ga = 0
        val results = mutableListOf<String>()
        var points = 0

        for (m in matches) {

            val isHome = m.homeTeam.equals(teamName, ignoreCase = true)

            val homeGoals = m.score?.fullTime?.home ?: 0
            val awayGoals = m.score?.fullTime?.away ?: 0

            val goalsFor = if (isHome) homeGoals else awayGoals
            val goalsAgainst = if (isHome) awayGoals else homeGoals

            gf += goalsFor
            ga += goalsAgainst

            val result = when {
                goalsFor > goalsAgainst -> "W"
                goalsFor < goalsAgainst -> "L"
                else -> "D"
            }

            results.add(result)

            points += when (result) {
                "W" -> 3
                "D" -> 1
                else -> 0
            }
        }

        val formScore = points * 0.7 + (gf - ga) * 0.3

        return RecentForm(results, gf, ga, points, formScore)
    }

    private fun computeProbabilities(
        home: TeamAggregateStats,
        away: TeamAggregateStats,
        homeForm: RecentForm,
        awayForm: RecentForm
    ): WinProbabilities {

        val ratingDiff = home.avgRating - away.avgRating
        val goalsDiff = home.totalGoals - away.totalGoals
        val formDiff = homeForm.formScore - awayForm.formScore

        var homeP = 0.33 + ratingDiff * 0.03 + goalsDiff * 0.02 + formDiff * 0.04
        var awayP = 0.33 - ratingDiff * 0.03 - goalsDiff * 0.02 - formDiff * 0.04
        var drawP = 0.34

        // Evitar negativos
        val raw = listOf(
            maxOf(0.0, homeP),
            maxOf(0.0, drawP),
            maxOf(0.0, awayP)
        )

        var sum = raw.sum()
        // Si t0do es cero, dar igual probabilidad
        val normalizedRaw = if (sum <= 0.0) listOf(1.0, 1.0, 1.0) else raw
        sum = normalizedRaw.sum()

        // Convertir a porcentajes
        val unrounded = normalizedRaw.map { it / sum * 100.0 }
        val rounded = unrounded.map { kotlin.math.round(it * 100.0) / 100.0 }.toMutableList() // 2 decimales

        // Ajustar la suma a 100% añadiendo la diferencia al más grande (evita problemas por redondeo)
        val diff = kotlin.math.round((100.0 - rounded.sum()) * 100.0) / 100.0
        if (diff != 0.0) {
            val maxIndex = rounded.indices.maxByOrNull { rounded[it] } ?: 0
            rounded[maxIndex] = kotlin.math.round((rounded[maxIndex] + diff) * 100.0) / 100.0
        }

        return WinProbabilities(
            homeWin = rounded[0],
            draw = rounded[1],
            awayWin = rounded[2]
        )
    }


    @VisibleForTesting
    internal fun computeProbabilitiesForTest(
        home: TeamAggregateStats,
        away: TeamAggregateStats,
        homeForm: RecentForm,
        awayForm: RecentForm
    ) = computeProbabilities(home, away, homeForm, awayForm)

    @VisibleForTesting
    internal fun computeRecentFormForTest(teamId: Long, teamName: String): RecentForm {
        return computeRecentForm(teamId, teamName)
    }
}