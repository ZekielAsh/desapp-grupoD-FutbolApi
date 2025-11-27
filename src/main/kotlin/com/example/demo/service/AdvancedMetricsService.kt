package com.example.demo.service

import com.example.demo.model.*
import com.example.demo.model.football.MatchesResponse
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import java.time.LocalDate

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
                season = getCurrentSeason(),
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

            // Use totalAverage data instead of summing competitions
            // This is important because matches format is like "10(2)" where (2) means substitute appearances
            val totalAvg = playerStats.totalAverage

            if (totalAvg == null) {
                return createEmptyPlayerMetrics(playerId, playerName)
            }

            // Parse matches - format is "X(Y)" where X is total matches and Y is substitute appearances
            val matchesText = totalAvg.matches
            val totalMatches = matchesText.split("(").firstOrNull()?.toIntOrNull() ?: 0

            val totalMinutes = totalAvg.minutes.toIntOrNull() ?: 0
            val totalGoals = totalAvg.goals.toIntOrNull() ?: 0
            val totalAssists = totalAvg.assists.toIntOrNull() ?: 0
            val totalYellowCards = totalAvg.yellowCards.toIntOrNull() ?: 0
            val totalRedCards = totalAvg.redCards.toIntOrNull() ?: 0
            val shotsPerGame = totalAvg.shotsPerGame.toDoubleOrNull() ?: 0.0
            val keyPassesPer90 = totalAvg.keyPasses.toDoubleOrNull() ?: 0.0
            val dribblesPer90 = totalAvg.dribbles.toDoubleOrNull() ?: 0.0
            val avgRating = totalAvg.rating.toDoubleOrNull() ?: 0.0

            // Basic metrics
            val goalsPerMatch = if (totalMatches > 0) (totalGoals.toDouble() / totalMatches).roundTo(2) else 0.0
            val assistsPerMatch = if (totalMatches > 0) (totalAssists.toDouble() / totalMatches).roundTo(2) else 0.0

            val minutesPer90 = totalMinutes / 90.0
            val goalsPer90 = if (minutesPer90 > 0) (totalGoals / minutesPer90).roundTo(2) else 0.0
            val assistsPer90 = if (minutesPer90 > 0) (totalAssists / minutesPer90).roundTo(2) else 0.0

            val goalContribution = totalGoals + totalAssists
            val goalContributionPer90 = if (minutesPer90 > 0) (goalContribution / minutesPer90).roundTo(2) else 0.0

            val minutesPerGoal = if (totalGoals > 0) (totalMinutes.toDouble() / totalGoals).roundTo(2) else null
            val minutesPerAssist = if (totalAssists > 0) (totalMinutes.toDouble() / totalAssists).roundTo(2) else null

            val yellowCardsPerMatch = if (totalMatches > 0) (totalYellowCards.toDouble() / totalMatches).roundTo(2) else 0.0
            val redCardsPerMatch = if (totalMatches > 0) (totalRedCards.toDouble() / totalMatches).roundTo(2) else 0.0

            // CALCULATED METRICS (not directly from WhoScored)

            // 1. Shot Accuracy: goals per shot ratio (higher = more clinical)
            val totalShots = if (totalMatches > 0) shotsPerGame * totalMatches else 0.0
            val shotAccuracy = if (totalShots > 0) ((totalGoals.toDouble() / totalShots) * 100).roundTo(2) else 0.0

            // 2. Creative Efficiency: assists per key pass ratio (how often key passes lead to goals)
            val totalKeyPasses = if (minutesPer90 > 0) keyPassesPer90 * minutesPer90 else 0.0
            val creativeEfficiency = if (totalKeyPasses > 0) ((totalAssists.toDouble() / totalKeyPasses) * 100).roundTo(2) else 0.0

            // 3. Dribble Success Rate: estimate based on dribbles per 90 (normalized)
            // Higher dribbles per 90 suggests successful dribbling
            val dribbleSuccessRate = ((dribblesPer90 / 5.0) * 100).coerceIn(0.0, 100.0).roundTo(2)

            // 4. Playing Time Percentage: how much of total possible minutes played
            val maxPossibleMinutes = totalMatches * 90.0
            val playingTimePercentage = if (maxPossibleMinutes > 0)
                ((totalMinutes.toDouble() / maxPossibleMinutes) * 100).roundTo(2) else 0.0

            val efficiency = EfficiencyMetrics(
                shotAccuracy = shotAccuracy,
                creativeEfficiency = creativeEfficiency,
                dribbleSuccessRate = dribbleSuccessRate,
                playingTimePercentage = playingTimePercentage
            )

            // 5. Impact Score: weighted combination of all offensive contributions
            // Formula: (goals * 3 + assists * 2 + keyPasses + dribbles) / matches * rating factor
            val rawImpact = if (totalMatches > 0) {
                ((totalGoals * 3.0 + totalAssists * 2.0 + totalKeyPasses + (dribblesPer90 * minutesPer90)) / totalMatches)
            } else 0.0
            val ratingFactor = avgRating / 10.0 // Normalize rating (typically 6-9) to 0.6-0.9
            val impactScore = (rawImpact * ratingFactor).roundTo(2)

            // 6. Versatility Index: measures how well-rounded a player is
            // Based on balance between goals, assists, key passes, dribbles
            val metrics = listOf(goalsPer90, assistsPer90, keyPassesPer90, dribblesPer90)
            val avgMetric = metrics.average()
            val variance = if (avgMetric > 0) {
                metrics.map { (it - avgMetric).let { diff -> diff * diff } }.average()
            } else 0.0
            // Lower variance = more balanced = higher versatility
            // Normalize to 0-100 scale
            val versatilityIndex = if (avgMetric > 0) {
                val coefficientOfVariation = kotlin.math.sqrt(variance) / avgMetric
                ((1 - coefficientOfVariation.coerceIn(0.0, 1.0)) * 100).roundTo(2)
            } else 0.0

            return PlayerAdvancedMetrics(
                playerId = playerId,
                playerName = playerName,
                season = getCurrentSeason(),
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
                ),
                efficiency = efficiency,
                impactScore = impactScore,
                versatilityIndex = versatilityIndex
            )

        } catch (ex: Exception) {
            throw RuntimeException("Failed to get player metrics for player $playerId: ${ex.message}")
        }
    }

    private fun createEmptyTeamMetrics(teamId: Long, teamName: String): TeamAdvancedMetrics {
        return TeamAdvancedMetrics(
            teamId = teamId,
            teamName = teamName,
            season = getCurrentSeason(),
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
            season = getCurrentSeason(),
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
            discipline = DisciplineData(0, 0, 0.0, 0.0),
            efficiency = EfficiencyMetrics(0.0, 0.0, 0.0, 0.0),
            impactScore = 0.0,
            versatilityIndex = 0.0
        )
    }

    /**
     * Calculates the current football season based on today's date.
     * Football seasons typically run from July/August to May/June.
     * - If current month is July (7) to December (12): season is currentYear/nextYear
     * - If current month is January (1) to June (6): season is previousYear/currentYear
     *
     * Example:
     * - 27/11/2025 → "2025/2026"
     * - 15/03/2025 → "2024/2025"
     * - 01/08/2025 → "2025/2026"
     */
    private fun getCurrentSeason(): String {
        val today = LocalDate.now()
        val year = today.year
        val month = today.monthValue

        return if (month >= 7) {
            // July to December: current season is year/year+1
            "$year/${year + 1}"
        } else {
            // January to June: current season is year-1/year
            "${year - 1}/$year"
        }
    }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}

