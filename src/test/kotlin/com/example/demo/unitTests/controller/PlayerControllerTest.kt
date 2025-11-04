package com.example.demo.unitTests.controller

import com.example.demo.controller.PlayerController
import com.example.demo.model.football.CompetitionStats
import com.example.demo.model.football.PlayerStatsResponse
import com.example.demo.model.football.StatsData
import com.example.demo.service.PlayerService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.junit.jupiter.api.Assertions.*

@ExtendWith(MockitoExtension::class)
class PlayerControllerTest {

    @Mock
    private lateinit var playerService: PlayerService

    @InjectMocks
    private lateinit var playerController: PlayerController

    @Test
    fun `test getPlayerStats returns statistics successfully`() {
        val playerId = "123456"
        val playerName = "lionel-messi"
        val competitions = listOf(
            CompetitionStats(
                "La Liga",
                StatsData("30", "2700", "25", "15", "2", "0", "5.0", "3.5", "6.0", "5", "9.0")
            )
        )
        val totalAverage = StatsData("30", "2700", "25", "15", "2", "0", "5.0", "3.5", "6.0", "5", "9.0")
        val statsResponse = PlayerStatsResponse(competitions, totalAverage)

        whenever(playerService.getPlayerStats(playerId, playerName)).thenReturn(statsResponse)

        val response = playerController.getPlayerStats(playerId, playerName)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        val stats = response.body as PlayerStatsResponse
        assertEquals(1, stats.competitions.size)
        assertNotNull(stats.totalAverage)
        verify(playerService).getPlayerStats(playerId, playerName)
    }

    @Test
    fun `test getPlayerStats returns empty list when no stats found`() {
        val playerId = "999999"
        val playerName = "unknown-player"
        val emptyResponse = PlayerStatsResponse(emptyList(), null)

        whenever(playerService.getPlayerStats(playerId, playerName)).thenReturn(emptyResponse)

        val response = playerController.getPlayerStats(playerId, playerName)

        assertEquals(HttpStatus.OK, response.statusCode)
        val stats = response.body as PlayerStatsResponse
        assertTrue(stats.competitions.isEmpty())
        assertNull(stats.totalAverage)
    }

    @Test
    fun `test getPlayerStats returns bad request on exception`() {
        val playerId = "error"
        val playerName = "error-player"

        whenever(playerService.getPlayerStats(playerId, playerName))
            .thenThrow(RuntimeException("Scraping failed"))

        val response = playerController.getPlayerStats(playerId, playerName)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body.toString().contains("Error retrieving player stats"))
    }

    @Test
    fun `test getPlayerStats with multiple competitions`() {
        val playerId = "100"
        val playerName = "multi-league-player"
        val competitions = listOf(
            CompetitionStats("Premier League", StatsData("20", "1800", "12", "8", "3", "0", "3.5", "2.5", "4.0", "1", "7.5")),
            CompetitionStats("FA Cup", StatsData("5", "450", "3", "2", "0", "0", "4.0", "3.0", "3.5", "0", "7.8")),
            CompetitionStats("Champions League", StatsData("8", "720", "5", "3", "1", "0", "4.5", "3.5", "5.0", "1", "8.0"))
        )
        val totalAverage = StatsData("33", "2970", "20", "13", "4", "0", "3.8", "2.8", "4.2", "2", "7.7")
        val statsResponse = PlayerStatsResponse(competitions, totalAverage)

        whenever(playerService.getPlayerStats(playerId, playerName)).thenReturn(statsResponse)

        val response = playerController.getPlayerStats(playerId, playerName)

        assertEquals(HttpStatus.OK, response.statusCode)
        val stats = response.body as PlayerStatsResponse
        assertEquals(3, stats.competitions.size)
        assertNotNull(stats.totalAverage)
    }

    @Test
    fun `test getPlayerStats with special characters in name`() {
        val playerId = "200"
        val playerName = "player-with-accent-é"
        val competitions = listOf(
            CompetitionStats("Ligue 1", StatsData("15", "1350", "8", "5", "2", "0", "3.0", "2.0", "3.5", "1", "7.2"))
        )
        val totalAverage = StatsData("15", "1350", "8", "5", "2", "0", "3.0", "2.0", "3.5", "1", "7.2")
        val statsResponse = PlayerStatsResponse(competitions, totalAverage)

        whenever(playerService.getPlayerStats(playerId, playerName)).thenReturn(statsResponse)

        val response = playerController.getPlayerStats(playerId, playerName)

        assertEquals(HttpStatus.OK, response.statusCode)
        verify(playerService).getPlayerStats(playerId, playerName)
    }
}

