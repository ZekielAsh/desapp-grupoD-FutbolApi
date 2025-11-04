package com.example.demo.controller

import com.example.demo.model.PlayerStats
import com.example.demo.model.TeamPlayersResponse
import com.example.demo.service.TeamService
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
class WhoScoredControllerTest {

    @Mock
    private lateinit var teamService: TeamService

    @InjectMocks
    private lateinit var whoScoredController: WhoScoredController

    @Test
    fun `test getTeamPlayers returns team players successfully`() {
        val teamName = "Barcelona"
        val players = listOf(
            PlayerStats("Lionel Messi", "30", 25, 20, 9.0),
            PlayerStats("Luis Suarez", "28", 18, 12, 8.5)
        )
        val response = TeamPlayersResponse(teamName, players)

        whenever(teamService.getTeamPlayersByName(teamName)).thenReturn(response)

        val result = whoScoredController.getTeamPlayers(teamName)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertNotNull(result.body)
        assertEquals("Barcelona", result.body!!.team)
        assertEquals(2, result.body!!.players.size)
        verify(teamService).getTeamPlayersByName(teamName)
    }

    @Test
    fun `test getTeamPlayers returns empty players list`() {
        val teamName = "Unknown Team"
        val response = TeamPlayersResponse(teamName, emptyList())

        whenever(teamService.getTeamPlayersByName(teamName)).thenReturn(response)

        val result = whoScoredController.getTeamPlayers(teamName)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertNotNull(result.body)
        assertTrue(result.body!!.players.isEmpty())
    }

    @Test
    fun `test getTeamPlayers returns bad request on exception`() {
        val teamName = "Error Team"

        whenever(teamService.getTeamPlayersByName(teamName))
            .thenThrow(RuntimeException("Scraping error"))

        val result = whoScoredController.getTeamPlayers(teamName)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertNull(result.body)
    }

    @Test
    fun `test getTeamPlayers with special characters in team name`() {
        val teamName = "Atlético Madrid"
        val response = TeamPlayersResponse(
            teamName,
            listOf(PlayerStats("Player", "10", 5, 3, 7.5))
        )

        whenever(teamService.getTeamPlayersByName(teamName)).thenReturn(response)

        val result = whoScoredController.getTeamPlayers(teamName)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(teamName, result.body!!.team)
    }

    @Test
    fun `test getTeamPlayers with multiple players having different stats`() {
        val teamName = "Real Madrid"
        val players = listOf(
            PlayerStats("Cristiano Ronaldo", "35", 30, 10, 9.5),
            PlayerStats("Karim Benzema", "33", 20, 15, 8.8),
            PlayerStats("Luka Modric", "32", 5, 12, 8.2),
            PlayerStats("Thibaut Courtois", "35", 0, 0, 7.5)
        )
        val response = TeamPlayersResponse(teamName, players)

        whenever(teamService.getTeamPlayersByName(teamName)).thenReturn(response)

        val result = whoScoredController.getTeamPlayers(teamName)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(4, result.body!!.players.size)
        assertEquals(30, result.body!!.players[0].goals)
        assertEquals(0, result.body!!.players[3].goals)
    }

    @Test
    fun `test getTeamPlayers with team name containing spaces`() {
        val teamName = "Manchester United"
        val response = TeamPlayersResponse(teamName, listOf())

        whenever(teamService.getTeamPlayersByName(teamName)).thenReturn(response)

        val result = whoScoredController.getTeamPlayers(teamName)

        assertEquals(HttpStatus.OK, result.statusCode)
        verify(teamService).getTeamPlayersByName(teamName)
    }
}

