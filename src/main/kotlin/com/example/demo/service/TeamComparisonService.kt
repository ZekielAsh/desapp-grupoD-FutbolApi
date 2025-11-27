package com.example.demo.service

import com.example.demo.model.*
import com.example.demo.model.football.MatchesResponse
import com.example.demo.model.football.StandingsResponse
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class TeamComparisonService(
    private val footballRestClient: RestClient,
    private val teamService: TeamService
) {

    @Cacheable("teamComparison", key = "#team1Id + '_' + #team2Id")
    fun compareTeams(team1Id: Long, team2Id: Long): TeamComparisonResponse {
        val team1Data = getTeamComparisonData(team1Id)
        val team2Data = getTeamComparisonData(team2Id)
        val headToHead = getHeadToHeadData(team1Id, team2Id, team1Data.name, team2Data.name)

        return TeamComparisonResponse(
            team1 = team1Data,
            team2 = team2Data,
            headToHead = headToHead
        )
    }

    private fun getTeamComparisonData(teamId: Long): TeamComparisonData {
        try {
            // Get team matches to calculate stats
            val matchesResponse = footballRestClient.get()
                .uri("/teams/{id}/matches?status=FINISHED&season=2024", teamId)
                .retrieve()
                .body(MatchesResponse::class.java)

            val matches = matchesResponse?.matches ?: emptyList()

            // Get team info
            val teamResponse = footballRestClient.get()
                .uri("/teams/{id}", teamId)
                .retrieve()
                .body(com.example.demo.model.football.TeamResponse::class.java)

            val teamName = teamResponse?.name ?: "Unknown"

            // Calculate stats from matches
            var wins = 0
            var draws = 0
            var losses = 0
            var goalsFor = 0
            var goalsAgainst = 0
            val formList = mutableListOf<String>()

            matches.sortedByDescending { it.utcDate }.take(5).forEach { match ->
                val isHome = match.homeTeam.id == teamId
                val teamGoals = if (isHome) match.score?.fullTime?.home ?: 0 else match.score?.fullTime?.away ?: 0
                val opponentGoals = if (isHome) match.score?.fullTime?.away ?: 0 else match.score?.fullTime?.home ?: 0

                goalsFor += teamGoals
                goalsAgainst += opponentGoals

                when {
                    teamGoals > opponentGoals -> {
                        wins++
                        formList.add("W")
                    }
                    teamGoals < opponentGoals -> {
                        losses++
                        formList.add("L")
                    }
                    else -> {
                        draws++
                        formList.add("D")
                    }
                }
            }

            val points = wins * 3 + draws
            val form = formList.take(5).joinToString("")

            // Try to get position from standings
            val position = try {
                getTeamPosition(teamId)
            } catch (e: Exception) {
                null
            }

            return TeamComparisonData(
                id = teamId,
                name = teamName,
                wins = wins,
                draws = draws,
                losses = losses,
                goalsFor = goalsFor,
                goalsAgainst = goalsAgainst,
                points = points,
                position = position,
                form = form
            )

        } catch (ex: RestClientResponseException) {
            throw RuntimeException("Failed to get team data for team $teamId: ${ex.message}")
        }
    }

    private fun getTeamPosition(teamId: Long): Int? {
        return try {
            val standingsResponse = footballRestClient.get()
                .uri("/teams/{id}/standings", teamId)
                .retrieve()
                .body(StandingsResponse::class.java)

            standingsResponse?.standings?.firstOrNull()?.table?.find { it.team.id == teamId }?.position
        } catch (e: Exception) {
            null
        }
    }

    private fun getHeadToHeadData(team1Id: Long, team2Id: Long, team1Name: String, team2Name: String): HeadToHeadData {
        try {
            // Get head-to-head matches
            val matchesResponse = footballRestClient.get()
                .uri("/teams/{id}/matches?status=FINISHED&season=2024", team1Id)
                .retrieve()
                .body(MatchesResponse::class.java)

            val allMatches = matchesResponse?.matches ?: emptyList()

            // Filter matches between the two teams
            val h2hMatches = allMatches.filter { match ->
                val homeId = match.homeTeam.id
                val awayId = match.awayTeam.id
                (homeId == team1Id && awayId == team2Id) ||
                (homeId == team2Id && awayId == team1Id)
            }

            var team1Wins = 0
            var team2Wins = 0
            var draws = 0
            val lastMeetings = mutableListOf<HeadToHeadMatch>()

            h2hMatches.sortedByDescending { it.utcDate }.take(5).forEach { match ->
                val homeScore = match.score?.fullTime?.home ?: 0
                val awayScore = match.score?.fullTime?.away ?: 0
                val homeId = match.homeTeam.id
                val awayId = match.awayTeam.id

                val winner = when {
                    homeScore > awayScore -> {
                        if (homeId == team1Id) {
                            team1Wins++
                            team1Name
                        } else {
                            team2Wins++
                            team2Name
                        }
                    }
                    homeScore < awayScore -> {
                        if (awayId == team1Id) {
                            team1Wins++
                            team1Name
                        } else {
                            team2Wins++
                            team2Name
                        }
                    }
                    else -> {
                        draws++
                        "Draw"
                    }
                }

                lastMeetings.add(
                    HeadToHeadMatch(
                        date = match.utcDate ?: "",
                        homeTeam = match.homeTeam.name ?: "",
                        awayTeam = match.awayTeam.name ?: "",
                        homeScore = homeScore,
                        awayScore = awayScore,
                        winner = winner
                    )
                )
            }

            return HeadToHeadData(
                team1Wins = team1Wins,
                team2Wins = team2Wins,
                draws = draws,
                totalMatches = lastMeetings.size,
                lastMeetings = lastMeetings
            )

        } catch (ex: RestClientResponseException) {
            // Return empty head-to-head if no data available
            return HeadToHeadData(
                team1Wins = 0,
                team2Wins = 0,
                draws = 0,
                totalMatches = 0,
                lastMeetings = emptyList()
            )
        }
    }
}

