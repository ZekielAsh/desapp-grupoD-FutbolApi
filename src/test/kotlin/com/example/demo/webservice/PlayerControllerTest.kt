package com.example.demo.webservice

import com.example.demo.model.football.CompetitionStats
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
        val stats = listOf(
            CompetitionStats(
                "La Liga",
                StatsData("30", "2700", "25", "15", "2", "0", "5.0", "3.5", "6.0", "5", "9.0")
            )
        )

        whenever(playerService.getPlayerStats(playerId, playerName)).thenReturn(stats)

        val response = playerController.getPlayerStats(playerId, playerName)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        val statsList = response.body as List<*>
        assertEquals(1, statsList.size)
        verify(playerService).getPlayerStats(playerId, playerName)
    }

    @Test
    fun `test getPlayerStats returns empty list when no stats found`() {
        val playerId = "999999"
        val playerName = "unknown-player"

        whenever(playerService.getPlayerStats(playerId, playerName)).thenReturn(emptyList())

        val response = playerController.getPlayerStats(playerId, playerName)

        assertEquals(HttpStatus.OK, response.statusCode)
        val statsList = response.body as List<*>
        assertTrue(statsList.isEmpty())
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
        val stats = listOf(
            CompetitionStats("Premier League", StatsData("20", "1800", "12", "8", "3", "0", "3.5", "2.5", "4.0", "1", "7.5")),
            CompetitionStats("FA Cup", StatsData("5", "450", "3", "2", "0", "0", "4.0", "3.0", "3.5", "0", "7.8")),
            CompetitionStats("Champions League", StatsData("8", "720", "5", "3", "1", "0", "4.5", "3.5", "5.0", "1", "8.0"))
        )

        whenever(playerService.getPlayerStats(playerId, playerName)).thenReturn(stats)

        val response = playerController.getPlayerStats(playerId, playerName)

        assertEquals(HttpStatus.OK, response.statusCode)
        val statsList = response.body as List<*>
        assertEquals(3, statsList.size)
    }

    @Test
    fun `test getPlayerStats with special characters in name`() {
        val playerId = "200"
        val playerName = "player-with-accent-é"
        val stats = listOf(
            CompetitionStats("Ligue 1", StatsData("15", "1350", "8", "5", "2", "0", "3.0", "2.0", "3.5", "1", "7.2"))
        )

        whenever(playerService.getPlayerStats(playerId, playerName)).thenReturn(stats)

        val response = playerController.getPlayerStats(playerId, playerName)

        assertEquals(HttpStatus.OK, response.statusCode)
        verify(playerService).getPlayerStats(playerId, playerName)
    }
}

