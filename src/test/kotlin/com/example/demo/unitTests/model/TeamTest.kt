package com.example.demo.unitTests.model

import com.example.demo.model.Player
import com.example.demo.model.Team
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TeamTest {

    @Test
    fun testTeamCreation() {
        val teamName = "Manchester City"
        val teamLeague = "Premier League"
        val teamCountry = "England"

        val team = Team(
            id = 1L,
            name = teamName,
            league = teamLeague,
            country = teamCountry
        )

        assertEquals(1L, team.id)
        assertEquals(teamName, team.name)
        assertEquals(teamLeague, team.league)
        assertEquals(teamCountry, team.country)
        assertTrue(team.players.isEmpty()) // Team starts with no players
    }

    @Test
    fun `test Team with players`() {
        val team = Team(
            name = "Barcelona",
            league = "La Liga",
            country = "Spain"
        )

        val player1 = Player(name = "Messi", position = "Forward")
        val player2 = Player(name = "Iniesta", position = "Midfielder")

        player1.team = team
        player2.team = team
        team.players.add(player1)
        team.players.add(player2)

        assertEquals(2, team.players.size)
        assertEquals("Messi", team.players[0].name)
        assertEquals("Iniesta", team.players[1].name)
    }

    @Test
    fun `test Team without id`() {
        val team = Team(
            name = "Real Madrid",
            league = "La Liga",
            country = "Spain"
        )

        assertNull(team.id)
        assertEquals("Real Madrid", team.name)
    }

    @Test
    fun `test Team property modification`() {
        val team = Team(
            id = 5L,
            name = "Original Name",
            league = "Original League",
            country = "Original Country"
        )

        team.name = "Modified Name"
        team.league = "Modified League"
        team.country = "Modified Country"

        assertEquals("Modified Name", team.name)
        assertEquals("Modified League", team.league)
        assertEquals("Modified Country", team.country)
    }

    @Test
    fun `test Team add and remove players`() {
        val team = Team(
            name = "Chelsea",
            league = "Premier League",
            country = "England"
        )

        val player = Player(name = "Hazard", position = "Winger")
        player.team = team
        team.players.add(player)

        assertEquals(1, team.players.size)

        team.players.remove(player)

        assertTrue(team.players.isEmpty())
    }

    @Test
    fun `test Team with empty players list`() {
        val team = Team(
            name = "New Team",
            league = "New League",
            country = "New Country",
            players = mutableListOf()
        )

        assertTrue(team.players.isEmpty())
        assertEquals(0, team.players.size)
    }
}