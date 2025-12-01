package com.example.demo.unitTests.controller

import com.example.demo.controller.WhoScoredController
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
        val teamResponse = result.body as TeamPlayersResponse
        assertEquals("Barcelona", teamResponse.team)
        assertEquals(2, teamResponse.players.size)
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
        val teamResponse = result.body as TeamPlayersResponse
        assertTrue(teamResponse.players.isEmpty())
    }

    @Test
    fun `test getTeamPlayers returns bad request on exception`() {
        val teamName = "Error Team"

        whenever(teamService.getTeamPlayersByName(teamName))
            .thenThrow(RuntimeException("Scraping error"))

        val result = whoScoredController.getTeamPlayers(teamName)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
        assertNotNull(result.body)
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
        val teamResponse = result.body as TeamPlayersResponse
        assertEquals(teamName, teamResponse.team)
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
        val teamResponse = result.body as TeamPlayersResponse
        assertEquals(4, teamResponse.players.size)
        assertEquals(30, teamResponse.players[0].goals)
        assertEquals(0, teamResponse.players[3].goals)
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

