package com.example.demo.service

import com.example.demo.model.*
import com.example.demo.model.football.MatchesResponse
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class AdvancedMetricsService(
    private val footballRestClient: RestClient,
    private val scrapperService: ScrapperService
) {

    @Cacheable("teamMetrics", key = "#teamId")
    fun getTeamAdvancedMetrics(teamId: Long): TeamAdvancedMetrics {
        try {
            // Get team info
            val teamResponse = footballRestClient.get()
                .uri("/teams/{id}", teamId)
                .retrieve()
                .body(com.example.demo.model.football.TeamResponse::class.java)

            val teamName = teamResponse?.name ?: "Unknown"

            // Get finished matches for current season
            val matchesResponse = footballRestClient.get()
                .uri("/teams/{id}/matches?status=FINISHED&season=2024", teamId)
                .retrieve()
                .body(MatchesResponse::class.java)

            val matches = matchesResponse?.matches ?: emptyList()

            if (matches.isEmpty()) {
                return createEmptyTeamMetrics(teamId, teamName)
            }

            // Calculate overall stats
            var totalGoalsScored = 0
            var totalGoalsConceded = 0
            var totalWins = 0
            var totalDraws = 0
            var totalLosses = 0
            var cleanSheets = 0

            // Home and away stats
            var homePlayed = 0
            var homeWins = 0
            var homeDraws = 0
            var homeLosses = 0
            var homeGoalsFor = 0
            var homeGoalsAgainst = 0

            var awayPlayed = 0
            var awayWins = 0
            var awayDraws = 0
            var awayLosses = 0
            var awayGoalsFor = 0
            var awayGoalsAgainst = 0

            // Recent form (last 5 matches)
            val recentMatches = matches.sortedByDescending { it.utcDate }.take(5)
            val formResults = mutableListOf<String>()
            var formPoints = 0
            var formGoalsScored = 0
            var formGoalsConceded = 0

            // Process all matches
            matches.forEach { match ->
                val isHome = match.homeTeam.id == teamId
                val teamGoals = if (isHome) match.score?.fullTime?.home ?: 0 else match.score?.fullTime?.away ?: 0
                val opponentGoals = if (isHome) match.score?.fullTime?.away ?: 0 else match.score?.fullTime?.home ?: 0

                totalGoalsScored += teamGoals
                totalGoalsConceded += opponentGoals

                if (opponentGoals == 0) cleanSheets++

                when {
                    teamGoals > opponentGoals -> totalWins++
                    teamGoals < opponentGoals -> totalLosses++
                    else -> totalDraws++
                }

                // Home/Away breakdown
                if (isHome) {
                    homePlayed++
                    homeGoalsFor += teamGoals
                    homeGoalsAgainst += opponentGoals
                    when {
                        teamGoals > opponentGoals -> homeWins++
                        teamGoals < opponentGoals -> homeLosses++
                        else -> homeDraws++
                    }
                } else {
                    awayPlayed++
                    awayGoalsFor += teamGoals
                    awayGoalsAgainst += opponentGoals
                    when {
                        teamGoals > opponentGoals -> awayWins++
                        teamGoals < opponentGoals -> awayLosses++
                        else -> awayDraws++
                    }
                }
            }

            // Process recent form
            recentMatches.forEach { match ->
                val isHome = match.homeTeam.id == teamId
                val teamGoals = if (isHome) match.score?.fullTime?.home ?: 0 else match.score?.fullTime?.away ?: 0
                val opponentGoals = if (isHome) match.score?.fullTime?.away ?: 0 else match.score?.fullTime?.home ?: 0

                formGoalsScored += teamGoals
                formGoalsConceded += opponentGoals

                val result = when {
                    teamGoals > opponentGoals -> {
                        formPoints += 3
                        "W"
                    }
                    teamGoals < opponentGoals -> "L"
                    else -> {
                        formPoints += 1
                        "D"
                    }
                }
                formResults.add(result)
            }

            val totalMatches = matches.size
            val winRate = if (totalMatches > 0) (totalWins.toDouble() / totalMatches * 100).roundTo(2) else 0.0
            val drawRate = if (totalMatches > 0) (totalDraws.toDouble() / totalMatches * 100).roundTo(2) else 0.0
            val lossRate = if (totalMatches > 0) (totalLosses.toDouble() / totalMatches * 100).roundTo(2) else 0.0
            val avgGoalsScored = if (totalMatches > 0) (totalGoalsScored.toDouble() / totalMatches).roundTo(2) else 0.0
            val avgGoalsConceded = if (totalMatches > 0) (totalGoalsConceded.toDouble() / totalMatches).roundTo(2) else 0.0

            // Calculate strength metrics (normalized 0-100)
            val attackStrength = ((avgGoalsScored / 3.0) * 100).coerceIn(0.0, 100.0).roundTo(2)
            val defenseStrength = (100 - ((avgGoalsConceded / 3.0) * 100)).coerceIn(0.0, 100.0).roundTo(2)

            val homeWinRate = if (homePlayed > 0) (homeWins.toDouble() / homePlayed * 100).roundTo(2) else 0.0
            val awayWinRate = if (awayPlayed > 0) (awayWins.toDouble() / awayPlayed * 100).roundTo(2) else 0.0

            val formScore = ((formPoints.toDouble() / 15) * 100).roundTo(2) // Max 15 points in 5 matches

            return TeamAdvancedMetrics(
                teamId = teamId,
                teamName = teamName,
                season = "2024",
                averageGoalsScored = avgGoalsScored,
                averageGoalsConceded = avgGoalsConceded,
                cleanSheets = cleanSheets,
                winRate = winRate,
                drawRate = drawRate,
                lossRate = lossRate,
                goalsPerMatch = avgGoalsScored,
                goalsConcededPerMatch = avgGoalsConceded,
                goalDifference = totalGoalsScored - totalGoalsConceded,
                homePerformance = PerformanceData(
                    played = homePlayed,
                    wins = homeWins,
                    draws = homeDraws,
                    losses = homeLosses,
                    goalsFor = homeGoalsFor,
                    goalsAgainst = homeGoalsAgainst,
                    points = homeWins * 3 + homeDraws,
                    winRate = homeWinRate
                ),
                awayPerformance = PerformanceData(
                    played = awayPlayed,
                    wins = awayWins,
                    draws = awayDraws,
                    losses = awayLosses,
                    goalsFor = awayGoalsFor,
                    goalsAgainst = awayGoalsAgainst,
                    points = awayWins * 3 + awayDraws,
                    winRate = awayWinRate
                ),
                recentForm = FormData(
                    last5Matches = formResults,
                    points = formPoints,
                    goalsScored = formGoalsScored,
                    goalsConceded = formGoalsConceded,
                    formScore = formScore
                ),
                attackStrength = attackStrength,
                defenseStrength = defenseStrength
            )

        } catch (ex: RestClientResponseException) {
            throw RuntimeException("Failed to get team metrics for team $teamId: ${ex.message}")
        }
    }

    @Cacheable("playerMetrics", key = "#playerId + '_' + #playerName")
    fun getPlayerAdvancedMetrics(playerId: String, playerName: String): PlayerAdvancedMetrics {
        try {
            // Get player stats from scraping service
            val playerStats = scrapperService.getPlayerSummaryStats(playerId, playerName)

            // Calculate metrics from competitions
            val allCompetitions = playerStats.competitions

            if (allCompetitions.isEmpty()) {
                return createEmptyPlayerMetrics(playerId, playerName)
            }

            var totalMatches = 0
            var totalMinutes = 0
            var totalGoals = 0
            var totalAssists = 0
            var totalYellowCards = 0
            var totalRedCards = 0
            var totalKeyPasses = 0.0
            var totalDribbles = 0.0
            var totalShots = 0.0
            var totalRating = 0.0
            var competitionCount = 0

            allCompetitions.forEach { comp ->
                val stats = comp.statistics

                totalMatches += stats.matches.toIntOrNull() ?: 0
                totalMinutes += stats.minutes.toIntOrNull() ?: 0
                totalGoals += stats.goals.toIntOrNull() ?: 0
                totalAssists += stats.assists.toIntOrNull() ?: 0
                totalYellowCards += stats.yellowCards.toIntOrNull() ?: 0
                totalRedCards += stats.redCards.toIntOrNull() ?: 0
                totalKeyPasses += stats.keyPasses.toDoubleOrNull() ?: 0.0
                totalDribbles += stats.dribbles.toDoubleOrNull() ?: 0.0
                totalShots += stats.shotsPerGame.toDoubleOrNull() ?: 0.0
                totalRating += stats.rating.toDoubleOrNull() ?: 0.0
                competitionCount++
            }

            val avgRating = if (competitionCount > 0) (totalRating / competitionCount).roundTo(2) else 0.0
            val goalsPerMatch = if (totalMatches > 0) (totalGoals.toDouble() / totalMatches).roundTo(2) else 0.0
            val assistsPerMatch = if (totalMatches > 0) (totalAssists.toDouble() / totalMatches).roundTo(2) else 0.0

            val minutesPer90 = totalMinutes / 90.0
            val goalsPer90 = if (minutesPer90 > 0) (totalGoals / minutesPer90).roundTo(2) else 0.0
            val assistsPer90 = if (minutesPer90 > 0) (totalAssists / minutesPer90).roundTo(2) else 0.0

            val goalContribution = totalGoals + totalAssists
            val goalContributionPer90 = if (minutesPer90 > 0) (goalContribution / minutesPer90).roundTo(2) else 0.0

            val keyPassesPer90 = if (minutesPer90 > 0) (totalKeyPasses / minutesPer90).roundTo(2) else 0.0
            val dribblesPer90 = if (minutesPer90 > 0) (totalDribbles / minutesPer90).roundTo(2) else 0.0
            val shotsPerGame = if (totalMatches > 0) (totalShots / totalMatches).roundTo(2) else 0.0

            val minutesPerGoal = if (totalGoals > 0) (totalMinutes.toDouble() / totalGoals).roundTo(2) else null
            val minutesPerAssist = if (totalAssists > 0) (totalMinutes.toDouble() / totalAssists).roundTo(2) else null

            val yellowCardsPerMatch = if (totalMatches > 0) (totalYellowCards.toDouble() / totalMatches).roundTo(2) else 0.0
            val redCardsPerMatch = if (totalMatches > 0) (totalRedCards.toDouble() / totalMatches).roundTo(2) else 0.0

            return PlayerAdvancedMetrics(
                playerId = playerId,
                playerName = playerName,
                season = "2024",
                totalMatches = totalMatches,
                totalMinutes = totalMinutes,
                goalsPerMatch = goalsPerMatch,
                assistsPerMatch = assistsPerMatch,
                goalsPer90 = goalsPer90,
                assistsPer90 = assistsPer90,
                goalContribution = goalContribution,
                goalContributionPer90 = goalContributionPer90,
                averageRating = avgRating,
                keyPassesPer90 = keyPassesPer90,
                dribblesPer90 = dribblesPer90,
                shotsPerGame = shotsPerGame,
                minutesPerGoal = minutesPerGoal,
                minutesPerAssist = minutesPerAssist,
                discipline = DisciplineData(
                    yellowCards = totalYellowCards,
                    redCards = totalRedCards,
                    yellowCardsPerMatch = yellowCardsPerMatch,
                    redCardsPerMatch = redCardsPerMatch
                )
            )

        } catch (ex: Exception) {
            throw RuntimeException("Failed to get player metrics for player $playerId: ${ex.message}")
        }
    }

    private fun createEmptyTeamMetrics(teamId: Long, teamName: String): TeamAdvancedMetrics {
        return TeamAdvancedMetrics(
            teamId = teamId,
            teamName = teamName,
            season = "2024",
            averageGoalsScored = 0.0,
            averageGoalsConceded = 0.0,
            cleanSheets = 0,
            winRate = 0.0,
            drawRate = 0.0,
            lossRate = 0.0,
            goalsPerMatch = 0.0,
            goalsConcededPerMatch = 0.0,
            goalDifference = 0,
            homePerformance = PerformanceData(0, 0, 0, 0, 0, 0, 0, 0.0),
            awayPerformance = PerformanceData(0, 0, 0, 0, 0, 0, 0, 0.0),
            recentForm = FormData(emptyList(), 0, 0, 0, 0.0),
            attackStrength = 0.0,
            defenseStrength = 0.0
        )
    }

    private fun createEmptyPlayerMetrics(playerId: String, playerName: String): PlayerAdvancedMetrics {
        return PlayerAdvancedMetrics(
            playerId = playerId,
            playerName = playerName,
            season = "2024",
            totalMatches = 0,
            totalMinutes = 0,
            goalsPerMatch = 0.0,
            assistsPerMatch = 0.0,
            goalsPer90 = 0.0,
            assistsPer90 = 0.0,
            goalContribution = 0,
            goalContributionPer90 = 0.0,
            averageRating = 0.0,
            keyPassesPer90 = 0.0,
            dribblesPer90 = 0.0,
            shotsPerGame = 0.0,
            minutesPerGoal = null,
            minutesPerAssist = null,
            discipline = DisciplineData(0, 0, 0.0, 0.0)
        )
    }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}

