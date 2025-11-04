package com.example.demo.unitTests.model.football

import com.example.demo.model.football.CompetitionDto
import com.example.demo.model.football.FullTimeScoreDto
import com.example.demo.model.football.MatchDto
import com.example.demo.model.football.MatchesResponse
import com.example.demo.model.football.PlayerDto
import com.example.demo.model.football.ScoreDto
import com.example.demo.model.football.TeamInfoDto
import com.example.demo.model.football.TeamResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TeamDtosTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun `test TeamResponse creation`() {
        val players = listOf(
            PlayerDto(1L, "Player 1", "Forward", "Spain", "1995-01-01", 10),
            PlayerDto(2L, "Player 2", "Midfielder", "Brazil", "1998-05-15", 8)
        )

        val team = TeamResponse(
            id = 100L,
            name = "FC Barcelona",
            squad = players
        )

        assertEquals(100L, team.id)
        assertEquals("FC Barcelona", team.name)
        assertEquals(2, team.squad.size)
    }

    @Test
    fun `test TeamResponse with empty squad`() {
        val team = TeamResponse(
            id = 200L,
            name = "New Team",
            squad = emptyList()
        )

        assertEquals(200L, team.id)
        assertTrue(team.squad.isEmpty())
    }

    @Test
    fun `test PlayerDto creation with all fields`() {
        val player = PlayerDto(
            id = 1L,
            name = "Lionel Messi",
            position = "Forward",
            nationality = "Argentina",
            dateOfBirth = "1987-06-24",
            shirtNumber = 10
        )

        assertEquals(1L, player.id)
        assertEquals("Lionel Messi", player.name)
        assertEquals("Forward", player.position)
        assertEquals("Argentina", player.nationality)
        assertEquals("1987-06-24", player.dateOfBirth)
        assertEquals(10, player.shirtNumber)
    }

    @Test
    fun `test PlayerDto with null fields`() {
        val player = PlayerDto(
            id = null,
            name = null,
            position = null,
            nationality = null,
            dateOfBirth = null,
            shirtNumber = null
        )

        assertNull(player.id)
        assertNull(player.name)
        assertNull(player.position)
    }

    @Test
    fun `test MatchesResponse with matches`() {
        val matches = listOf(
            MatchDto("La Liga", "Barcelona", "Real Madrid", "2025-11-15T20:00:00Z", null),
            MatchDto("Champions League", "Bayern", "PSG", "2025-11-20T21:00:00Z", null)
        )

        val response = MatchesResponse(matches = matches)

        assertEquals(2, response.matches.size)
        assertEquals("La Liga", response.matches[0].competitionName)
    }

    @Test
    fun `test CompetitionDto creation`() {
        val competition = CompetitionDto(name = "Premier League")

        assertEquals("Premier League", competition.name)
    }

    @Test
    fun `test TeamInfoDto creation`() {
        val teamInfo = TeamInfoDto(
            name = "Manchester City",
            shortName = "Man City",
            tla = "MCI",
            crest = "https://example.com/crest.png"
        )

        assertEquals("Manchester City", teamInfo.name)
        assertEquals("Man City", teamInfo.shortName)
        assertEquals("MCI", teamInfo.tla)
        assertEquals("https://example.com/crest.png", teamInfo.crest)
    }

    @Test
    fun `test MatchDto with JsonCreator constructor`() {
        val competition = CompetitionDto("Serie A")
        val homeTeam = TeamInfoDto("Juventus", "Juve", "JUV", null)
        val awayTeam = TeamInfoDto("Inter", "Inter", "INT", null)
        val score = ScoreDto(
            fullTime = FullTimeScoreDto(home = 2, away = 1)
        )

        val match = MatchDto(
            competition = competition,
            homeTeamDto = homeTeam,
            awayTeamDto = awayTeam,
            utcDate = "2025-11-10T18:00:00Z",
            scoreDto = score
        )

        assertEquals("Serie A", match.competitionName)
        assertEquals("Juventus", match.homeTeam)
        assertEquals("Inter", match.awayTeam)
        assertEquals("2025-11-10T18:00:00Z", match.utcDate)
        assertNotNull(match.score)
        assertEquals(2, match.score?.fullTime?.home)
        assertEquals(1, match.score?.fullTime?.away)

    }

    @Test
    fun `test MatchDto with null values`() {
        val match = MatchDto(
            competitionName = null,
            homeTeam = null,
            awayTeam = null,
            utcDate = null,
            score = null
        )

        assertNull(match.competitionName)
        assertNull(match.homeTeam)
        assertNull(match.awayTeam)
        assertNull(match.utcDate)
    }

    @Test
    fun `test TeamResponse JSON deserialization ignores unknown fields`() {
        val json = """
            {
                "id": 1,
                "name": "Test Team",
                "squad": [],
                "unknownField": "should be ignored"
            }
        """.trimIndent()

        val team = mapper.readValue(json, TeamResponse::class.java)

        assertEquals(1L, team.id)
        assertEquals("Test Team", team.name)
        assertTrue(team.squad.isEmpty())
    }

    @Test
    fun `test PlayerDto JSON deserialization`() {
        val json = """
            {
                "id": 123,
                "name": "Test Player",
                "position": "Goalkeeper",
                "nationality": "England",
                "dateOfBirth": "1990-01-01",
                "shirtNumber": 1
            }
        """.trimIndent()

        val player = mapper.readValue(json, PlayerDto::class.java)

        assertEquals(123L, player.id)
        assertEquals("Test Player", player.name)
        assertEquals("Goalkeeper", player.position)
    }
}

