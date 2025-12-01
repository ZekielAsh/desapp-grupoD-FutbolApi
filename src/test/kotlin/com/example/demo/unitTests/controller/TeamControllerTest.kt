package com.example.demo.unitTests.controller

import com.example.demo.controller.TeamController
import com.example.demo.model.football.*
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
            MatchDto(
                competitionName = "La Liga",
                homeTeam = TeamInfoDto(id = 81, name = "Barcelona", shortName = "FCB", crest = "url"),
                awayTeam = TeamInfoDto(id = 86, name = "Real Madrid", shortName = "RMA", crest = "url"),
                utcDate = "2025-11-15T20:00:00Z",
                score = null
            ),
            MatchDto(
                competitionName = "Champions League",
                homeTeam = TeamInfoDto(id = 81, name = "Barcelona", shortName = "FCB", crest = "url"),
                awayTeam = TeamInfoDto(id = 5, name = "Bayern", shortName = "FCB", crest = "url"),
                utcDate = "2025-11-20T21:00:00Z",
                score = null
            )
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

    @Test
    fun `getPlayers returns bad request on exception`() {
        val teamId = 100L

        whenever(teamService.getPlayers(teamId))
            .thenThrow(RuntimeException("Unexpected error"))

        val response = teamController.getPlayers(teamId)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body!!.isEmpty())
    }

    @Test
    fun `test getNextMatches returns matches including score`() {
        val teamId = 65L
        val match = MatchDto(
            competitionName = "Liga",
            homeTeam = TeamInfoDto(id = 81, name = "Barcelona", shortName = "FCB", crest = "url"),
            awayTeam = TeamInfoDto(id = 70, name = "Sevilla", shortName = "SEV", crest = "url"),
            utcDate = "2025-11-10T18:00:00Z",
            score = ScoreDto(fullTime = FullTimeScoreDto(2, 1))
        )

        whenever(teamService.getNextMatchesByTeamName(teamId)).thenReturn(listOf(match))

        val response = teamController.getNextMatches(teamId)
        val matchList = response.body as List<*>
        val result = matchList[0]

        assertEquals("Barcelona", (result as MatchDto).homeTeam.name)
        assertEquals(2, result.score?.fullTime?.home)
    }

    @Test
    fun `getNextMatches returns bad request when teamId is invalid`() {
        val response = teamController.getNextMatches(-5)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body.toString().contains("Invalid team ID"))
    }

    @Test
    fun `test calling getPlayers and getNextMatches in sequence`() {
        val teamId = 10L
        val players = listOf(PlayerDto(1, "X", "Forward", "AR", "2000-01-01", 9))
        val matches = listOf(
            MatchDto(
                competitionName = "Liga",
                homeTeam = TeamInfoDto(id = 10, name = "Team", shortName = "TM", crest = "url"),
                awayTeam = TeamInfoDto(id = 11, name = "Rival", shortName = "RIV", crest = "url"),
                utcDate = "2025-01-01",
                score = null
            )
        )

        whenever(teamService.getPlayers(teamId)).thenReturn(players)
        whenever(teamService.getNextMatchesByTeamName(teamId)).thenReturn(matches)

        val resp1 = teamController.getPlayers(teamId)
        val resp2 = teamController.getNextMatches(teamId)

        val matchList = resp2.body as List<MatchDto>

        assertEquals(1, resp1.body!!.size)
        assertEquals(1, matchList.size)

        verify(teamService).getPlayers(teamId)
        verify(teamService).getNextMatchesByTeamName(teamId)
    }
}
