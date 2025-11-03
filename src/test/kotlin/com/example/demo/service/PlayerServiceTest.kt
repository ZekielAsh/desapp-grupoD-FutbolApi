package com.example.demo.service

import com.example.demo.model.football.CompetitionStats
import com.example.demo.model.football.StatsData
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
        val expectedStats = listOf(
            CompetitionStats(
                "La Liga",
                StatsData("30", "2700", "25", "15", "2", "0", "5.0", "3.5", "6.0", "5", "9.0")
            ),
            CompetitionStats(
                "Champions League",
                StatsData("10", "900", "8", "5", "1", "0", "4.5", "3.0", "5.5", "2", "8.5")
            )
        )

        whenever(scrapperService.getPlayerSummaryStats(playerId, playerName)).thenReturn(expectedStats)

        val result = playerService.getPlayerStats(playerId, playerName)

        assertEquals(2, result.size)
        assertEquals("La Liga", result[0].campeonato)
        assertEquals("25", result[0].estadisticas.goles)
        assertEquals("Champions League", result[1].campeonato)
        verify(scrapperService).getPlayerSummaryStats(playerId, playerName)
    }

    @Test
    fun `test getPlayerStats returns empty list when no stats found`() {
        val playerId = "999999"
        val playerName = "unknown-player"

        whenever(scrapperService.getPlayerSummaryStats(playerId, playerName)).thenReturn(emptyList())

        val result = playerService.getPlayerStats(playerId, playerName)

        assertTrue(result.isEmpty())
        verify(scrapperService).getPlayerSummaryStats(playerId, playerName)
    }

    @Test
    fun `test getPlayerStats with single competition`() {
        val playerId = "100"
        val playerName = "player-name"
        val stats = listOf(
            CompetitionStats(
                "Premier League",
                StatsData("20", "1800", "12", "8", "3", "0", "3.5", "2.5", "4.0", "1", "7.5")
            )
        )

        whenever(scrapperService.getPlayerSummaryStats(playerId, playerName)).thenReturn(stats)

        val result = playerService.getPlayerStats(playerId, playerName)

        assertEquals(1, result.size)
        assertEquals("Premier League", result[0].campeonato)
        assertEquals("12", result[0].estadisticas.goles)
    }
}

