package com.example.demo.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayerTest {

    @Test
    fun testPlayerCreation() {
        val playerName = "Julian Alvarez"
        val playerPosition = "Forward"

        val player = Player(
            id = 1L,
            name = playerName,
            position = playerPosition
        )

        assertEquals(1L, player.id)
        assertEquals(playerName, player.name)
        assertEquals(playerPosition, player.position)
        assertNull(player.team, "Doesn't have a team assigned yet")
    }

    @Test
    fun `test Player creation without id`() {
        val player = Player(
            name = "Cristiano Ronaldo",
            position = "Forward"
        )

        assertNull(player.id)
        assertEquals("Cristiano Ronaldo", player.name)
        assertEquals("Forward", player.position)
    }

    @Test
    fun `test Player with team assignment`() {
        val team = Team(
            id = 10L,
            name = "Manchester United",
            league = "Premier League",
            country = "England"
        )

        val player = Player(
            name = "Bruno Fernandes",
            position = "Midfielder"
        )

        player.team = team

        assertNotNull(player.team)
        assertEquals("Manchester United", player.team?.name)
    }

    @Test
    fun `test Player property modification`() {
        val player = Player(
            id = 5L,
            name = "Original Name",
            position = "Original Position"
        )

        player.name = "Modified Name"
        player.position = "Modified Position"
        player.id = 10L

        assertEquals(10L, player.id)
        assertEquals("Modified Name", player.name)
        assertEquals("Modified Position", player.position)
    }

    @Test
    fun `test Player with different positions`() {
        val goalkeeper = Player(name = "Courtois", position = "Goalkeeper")
        val defender = Player(name = "Ramos", position = "Defender")
        val midfielder = Player(name = "Modric", position = "Midfielder")
        val forward = Player(name = "Benzema", position = "Forward")

        assertEquals("Goalkeeper", goalkeeper.position)
        assertEquals("Defender", defender.position)
        assertEquals("Midfielder", midfielder.position)
        assertEquals("Forward", forward.position)
    }

    @Test
    fun `test Player team can be changed`() {
        val team1 = Team(name = "Team A", league = "League A", country = "Country A")
        val team2 = Team(name = "Team B", league = "League B", country = "Country B")

        val player = Player(name = "Transfer Player", position = "Striker")

        player.team = team1
        assertEquals("Team A", player.team?.name)

        player.team = team2
        assertEquals("Team B", player.team?.name)
    }

    @Test
    fun `test Player team can be set to null`() {
        val team = Team(name = "Some Team", league = "Some League", country = "Some Country")
        val player = Player(name = "Free Agent", position = "Defender")

        player.team = team
        assertNotNull(player.team)

        player.team = null
        assertNull(player.team)
    }
}