package com.example.demo.controller

import com.example.demo.model.football.MatchDto
import com.example.demo.model.football.PlayerDto
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
class TeamControllerTest {

    @Mock
    private lateinit var teamService: TeamService

    @InjectMocks
    private lateinit var teamController: TeamController

    @Test
    fun `test getPlayers returns players successfully`() {
        val teamId = 65L
        val players = listOf(
            PlayerDto(1L, "Player 1", "Forward", "Spain", "1990-01-01", 10),
            PlayerDto(2L, "Player 2", "Midfielder", "Brazil", "1992-05-15", 8)
        )

        whenever(teamService.getPlayers(teamId)).thenReturn(players)

        val response = teamController.getPlayers(teamId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(2, response.body!!.size)
        assertEquals("Player 1", response.body!![0].name)
        verify(teamService).getPlayers(teamId)
    }

    @Test
    fun `test getPlayers returns empty list when no players found`() {
        val teamId = 999L

        whenever(teamService.getPlayers(teamId)).thenReturn(emptyList())

        val response = teamController.getPlayers(teamId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body!!.isEmpty())
    }

    @Test
    fun `test getNextMatches returns matches successfully`() {
        val teamId = 65L
        val matches = listOf(
            MatchDto("La Liga", "Barcelona", "Real Madrid", "2025-11-15T20:00:00Z"),
            MatchDto("Champions League", "Barcelona", "Bayern", "2025-11-20T21:00:00Z")
        )

        whenever(teamService.getNextMatchesByTeamName(teamId)).thenReturn(matches)

        val response = teamController.getNextMatches(teamId)

        assertEquals(HttpStatus.OK, response.statusCode)
        val matchList = response.body as List<*>
        assertEquals(2, matchList.size)
        verify(teamService).getNextMatchesByTeamName(teamId)
    }

    @Test
    fun `test getNextMatches returns empty list when no matches found`() {
        val teamId = 100L

        whenever(teamService.getNextMatchesByTeamName(teamId)).thenReturn(emptyList())

        val response = teamController.getNextMatches(teamId)

        assertEquals(HttpStatus.OK, response.statusCode)
        val matchList = response.body as List<*>
        assertTrue(matchList.isEmpty())
    }

    @Test
    fun `test getNextMatches returns bad request on exception`() {
        val teamId = 999L

        whenever(teamService.getNextMatchesByTeamName(teamId))
            .thenThrow(RuntimeException("Service error"))

        val response = teamController.getNextMatches(teamId)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body.toString().contains("Error retrieving next matches"))
    }

    @Test
    fun `test getPlayers with multiple teams`() {
        val team1Players = listOf(PlayerDto(1L, "Player A", "Forward", "Spain", "1990-01-01", 9))
        val team2Players = listOf(PlayerDto(2L, "Player B", "Defender", "France", "1988-03-10", 4))

        whenever(teamService.getPlayers(1L)).thenReturn(team1Players)
        whenever(teamService.getPlayers(2L)).thenReturn(team2Players)

        val response1 = teamController.getPlayers(1L)
        val response2 = teamController.getPlayers(2L)

        assertEquals("Player A", response1.body!![0].name)
        assertEquals("Player B", response2.body!![0].name)
    }
}

