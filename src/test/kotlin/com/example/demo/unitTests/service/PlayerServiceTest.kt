package com.example.demo.unitTests.service

import com.example.demo.model.football.CompetitionStats
import com.example.demo.model.football.PlayerStatsResponse
import com.example.demo.model.football.StatsData
import com.example.demo.service.PlayerService
import com.example.demo.service.ScrapperService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Assertions.*

@ExtendWith(MockitoExtension::class)
class PlayerServiceTest {

    @Mock
    private lateinit var scrapperService: ScrapperService

    @InjectMocks
    private lateinit var playerService: PlayerService

    @Test
    fun `test getPlayerStats returns statistics successfully`() {
        val playerId = "123456"
        val playerName = "lionel-messi"
        val competitions = listOf(
            CompetitionStats(
                "La Liga",
                StatsData("30", "2700", "25", "15", "2", "0", "5.0", "3.5", "6.0", "5", "9.0")
            ),
            CompetitionStats(
                "Champions League",
                StatsData("10", "900", "8", "5", "1", "0", "4.5", "3.0", "5.5", "2", "8.5")
            )
        )
        val totalAverage = StatsData("40", "3600", "33", "20", "3", "0", "4.8", "3.3", "5.8", "7", "8.8")
        val expectedStats = PlayerStatsResponse(competitions, totalAverage)

        whenever(scrapperService.getPlayerSummaryStats(playerId, playerName)).thenReturn(expectedStats)

        val result = playerService.getPlayerStats(playerId, playerName)

        assertEquals(2, result.competitions.size)
        assertEquals("La Liga", result.competitions[0].competition)
        assertEquals("25", result.competitions[0].statistics.goals)
        assertEquals("Champions League", result.competitions[1].competition)
        assertNotNull(result.totalAverage)
        assertEquals("33", result.totalAverage?.goals)
        verify(scrapperService).getPlayerSummaryStats(playerId, playerName)
    }

    @Test
    fun `test getPlayerStats returns empty list when no stats found`() {
        val playerId = "999999"
        val playerName = "unknown-player"
        val emptyStats = PlayerStatsResponse(emptyList(), null)

        whenever(scrapperService.getPlayerSummaryStats(playerId, playerName)).thenReturn(emptyStats)

        val result = playerService.getPlayerStats(playerId, playerName)

        assertTrue(result.competitions.isEmpty())
        assertNull(result.totalAverage)
        verify(scrapperService).getPlayerSummaryStats(playerId, playerName)
    }

    @Test
    fun `test getPlayerStats with single competition`() {
        val playerId = "100"
        val playerName = "player-name"
        val competitions = listOf(
            CompetitionStats(
                "Premier League",
                StatsData("20", "1800", "12", "8", "3", "0", "3.5", "2.5", "4.0", "1", "7.5")
            )
        )
        val totalAverage = StatsData("20", "1800", "12", "8", "3", "0", "3.5", "2.5", "4.0", "1", "7.5")
        val stats = PlayerStatsResponse(competitions, totalAverage)

        whenever(scrapperService.getPlayerSummaryStats(playerId, playerName)).thenReturn(stats)

        val result = playerService.getPlayerStats(playerId, playerName)

        assertEquals(1, result.competitions.size)
        assertEquals("Premier League", result.competitions[0].competition)
        assertEquals("12", result.competitions[0].statistics.goals)
        assertNotNull(result.totalAverage)
    }
}

