package com.example.demo.unitTests.service

import com.example.demo.model.PlayerStats
import com.example.demo.model.TeamPlayersResponse
import com.example.demo.model.football.*
import com.example.demo.service.ScrapperService
import com.example.demo.service.TeamService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.junit.jupiter.api.Assertions.*

@ExtendWith(MockitoExtension::class)
class TeamServiceTest {

    @Mock
    private lateinit var footballRestClient: RestClient

    @Mock
    private lateinit var scrapperService: ScrapperService

    @Mock
    private lateinit var requestHeadersUriSpec: RestClient.RequestHeadersUriSpec<*>

    @Mock
    private lateinit var responseSpec: RestClient.ResponseSpec

    @InjectMocks
    private lateinit var teamService: TeamService

    @Test
    fun `test getPlayers returns squad successfully`() {
        val teamId = 65L
        val players = listOf(
            PlayerDto(1L, "Player 1", "Forward", "Spain", "1990-01-01", 10),
            PlayerDto(2L, "Player 2", "Midfielder", "Brazil", "1992-05-15", 8)
        )
        val teamResponse = TeamResponse(teamId, "Barcelona", players)

        whenever(footballRestClient.get()).thenReturn(requestHeadersUriSpec as RestClient.RequestHeadersUriSpec<*>)
        whenever(requestHeadersUriSpec.uri(any<String>(), any<Long>())).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.body(TeamResponse::class.java)).thenReturn(teamResponse)

        val result = teamService.getPlayers(teamId)

        assertEquals(2, result.size)
        assertEquals("Player 1", result[0].name)
        verify(footballRestClient).get()
    }

    @Test
    fun `test getPlayers returns empty list when team not found`() {
        val teamId = 999L

        whenever(footballRestClient.get()).thenReturn(requestHeadersUriSpec as RestClient.RequestHeadersUriSpec<*>)
        whenever(requestHeadersUriSpec.uri(any<String>(), any<Long>())).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.body(TeamResponse::class.java))
            .thenThrow(RestClientResponseException("Not Found", HttpStatus.NOT_FOUND.value(), "", null, null, null))

        val result = teamService.getPlayers(teamId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `test getPlayers returns empty list when response is null`() {
        val teamId = 100L

        whenever(footballRestClient.get()).thenReturn(requestHeadersUriSpec as RestClient.RequestHeadersUriSpec<*>)
        whenever(requestHeadersUriSpec.uri(any<String>(), any<Long>())).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.body(TeamResponse::class.java)).thenReturn(null)

        val result = teamService.getPlayers(teamId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `test getTeamPlayersByName delegates to scrapper service`() {
        val teamName = "Barcelona"
        val expectedResponse = TeamPlayersResponse(
            team = "Barcelona",
            players = listOf(PlayerStats("Messi", "30", 25, 20, 9.0))
        )

        whenever(scrapperService.getTeamPlayersByName(teamName)).thenReturn(expectedResponse)

        val result = teamService.getTeamPlayersByName(teamName)

        assertEquals("Barcelona", result.team)
        assertEquals(1, result.players.size)
        verify(scrapperService).getTeamPlayersByName(teamName)
    }

    @Test
    fun `test getNextMatchesByTeamName returns matches successfully`() {
        val teamId = 65L
        val matches = listOf(
            MatchDto(
                competitionName = "La Liga",
                homeTeam = TeamInfoDto(id = 65, name = "Barcelona", shortName = "FCB", crest = null),
                awayTeam = TeamInfoDto(id = 86, name = "Real Madrid", shortName = "RMA", crest = null),
                utcDate = "2025-11-15T20:00:00Z",
                score = null
            ),
            MatchDto(
                competitionName = "Champions League",
                homeTeam = TeamInfoDto(id = 65, name = "Barcelona", shortName = "FCB", crest = null),
                awayTeam = TeamInfoDto(id = 5, name = "Bayern", shortName = "FCB", crest = null),
                utcDate = "2025-11-20T21:00:00Z",
                score = null
            )
        )
        val matchesResponse = MatchesResponse(matches)

        whenever(footballRestClient.get()).thenReturn(requestHeadersUriSpec as RestClient.RequestHeadersUriSpec<*>)
        whenever(requestHeadersUriSpec.uri(any<String>(), any<Long>())).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.body(MatchesResponse::class.java)).thenReturn(matchesResponse)

        val result = teamService.getNextMatchesByTeamName(teamId)

        assertEquals(2, result.size)
        assertEquals("La Liga", result[0].competitionName)
    }

    @Test
    fun `test getNextMatchesByTeamName returns empty list when not found`() {
        val teamId = 999L

        whenever(footballRestClient.get()).thenReturn(requestHeadersUriSpec as RestClient.RequestHeadersUriSpec<*>)
        whenever(requestHeadersUriSpec.uri(any<String>(), any<Long>())).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.body(MatchesResponse::class.java))
            .thenThrow(RestClientResponseException("Not Found", HttpStatus.NOT_FOUND.value(), "", null, null, null))

        val result = teamService.getNextMatchesByTeamName(teamId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `test getNextMatchesByTeamName returns empty list when response is null`() {
        val teamId = 100L

        whenever(footballRestClient.get()).thenReturn(requestHeadersUriSpec as RestClient.RequestHeadersUriSpec<*>)
        whenever(requestHeadersUriSpec.uri(any<String>(), any<Long>())).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.body(MatchesResponse::class.java)).thenReturn(null)

        val result = teamService.getNextMatchesByTeamName(teamId)

        assertTrue(result.isEmpty())
    }
}

