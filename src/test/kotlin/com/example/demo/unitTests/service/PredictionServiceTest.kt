package com.example.demo.unitTests.service

import com.example.demo.model.TeamPlayersResponse
import com.example.demo.model.PlayerStats
import com.example.demo.model.prediction.RecentForm
import com.example.demo.model.prediction.TeamAggregateStats
import com.example.demo.model.football.FullTimeScoreDto
import com.example.demo.model.football.ScoreDto
import com.example.demo.model.football.MatchDto
import com.example.demo.service.PredictionService
import com.example.demo.service.ScrapperService
import com.example.demo.service.TeamService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify

class PredictionServiceTest {

    private val scrapperService: ScrapperService = mock()
    private val teamService: TeamService = mock()

    private val svc = PredictionService(scrapperService, teamService)

    private fun samplePlayers(rating: Double, goals: Int, assists: Int, n: Int = 3) =
        (1..n).map {
            PlayerStats(
                name = "P$it",
                appearances = "10",
                goals = goals,
                assists = assists,
                rating = rating
            )
        }

    @Test
    fun `predictMatch returns valid result`() {
        val homeTeamName = "Home FC"
        val awayTeamName = "Away FC"
        val homeTeamId = 1L
        val awayTeamId = 2L

        val homePlayers = TeamPlayersResponse(team = homeTeamName, players = samplePlayers(7.5, 10, 5))
        val awayPlayers = TeamPlayersResponse(team = awayTeamName, players = samplePlayers(6.8, 6, 4))

        whenever(scrapperService.getTeamPlayersByName(homeTeamName)).thenReturn(homePlayers)
        whenever(scrapperService.getTeamPlayersByName(awayTeamName)).thenReturn(awayPlayers)

        // Partidos finalizados
        val m1 = MatchDto(
            competitionName = "League",
            homeTeam = "Home FC",
            awayTeam = "Other",
            utcDate = "2025-01-01",
            score = ScoreDto(fullTime = FullTimeScoreDto(home = 2, away = 1))
        )
        val m2 = MatchDto(
            competitionName = "League",
            homeTeam = "Other",
            awayTeam = "Home FC",
            utcDate = "2025-01-02",
            score = ScoreDto(fullTime = FullTimeScoreDto(home = 0, away = 1))
        )

        val mAway = MatchDto(
            competitionName = "League",
            homeTeam = "Away FC",
            awayTeam = "Other",
            utcDate = "2025-01-01",
            score = ScoreDto(fullTime = FullTimeScoreDto(home = 1, away = 1))
        )

        whenever(teamService.getLastFinishedMatches(homeTeamId)).thenReturn(listOf(m1, m2))
        whenever(teamService.getLastFinishedMatches(awayTeamId)).thenReturn(listOf(mAway))

        val result = svc.predictMatch(homeTeamName, awayTeamName, homeTeamId, awayTeamId)

        assertEquals("Home FC", result.homeTeam)
        assertNotNull(result.statComparison)
        assertNotNull(result.probabilities)
        assertTrue(result.probabilities.homeWin in 0.0..1.0)

        verify(scrapperService).getTeamPlayersByName(homeTeamName)
        verify(scrapperService).getTeamPlayersByName(awayTeamName)
        verify(teamService).getLastFinishedMatches(homeTeamId)
        verify(teamService).getLastFinishedMatches(awayTeamId)
    }

    @Test
    fun `probabilities sum to 1`() {
        val homeStats = TeamAggregateStats(8.0, 20, 10)
        val awayStats = TeamAggregateStats(7.0, 10, 5)

        val homeForm = RecentForm(listOf("W","W"), 4, 1, 6, 5.0)
        val awayForm = RecentForm(listOf("L","D"), 1, 3, 1, 0.5)

        val probs = svc.computeProbabilitiesForTest(homeStats, awayStats, homeForm, awayForm)

        assertEquals(1.0, probs.homeWin + probs.draw + probs.awayWin, 1e-6)
    }

    @Test
    fun `computeRecentForm calculates correct totals`() {
        val teamId = 10L

        val m1 = MatchDto(
            competitionName = "C",
            homeTeam = "T",
            awayTeam = "Other",
            utcDate = "2025-01-01",
            score = ScoreDto(fullTime = FullTimeScoreDto(2, 0))
        )
        val m2 = MatchDto(
            competitionName = "C",
            homeTeam = "Other",
            awayTeam = "T",
            utcDate = "2025-01-02",
            score = ScoreDto(fullTime = FullTimeScoreDto(1, 1))
        )

        whenever(teamService.getLastFinishedMatches(teamId)).thenReturn(listOf(m1, m2))

        val recent = svc.computeRecentFormForTest(teamId, "T")

        assertEquals(2, recent.results.size)
        assertEquals(3, recent.goalsFor)
        assertEquals(1, recent.goalsAgainst)
        assertEquals(4, recent.points)
    }
}
