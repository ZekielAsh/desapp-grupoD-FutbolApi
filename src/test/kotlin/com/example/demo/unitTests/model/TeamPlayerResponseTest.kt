package com.example.demo.unitTests.model

import com.example.demo.model.PlayerStats
import com.example.demo.model.TeamPlayersResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TeamPlayersResponseTest {

    @Test
    fun `test TeamPlayersResponse creation with players`() {
        val players = listOf(
            PlayerStats("Player 1", "10", 5, 3, 7.5),
            PlayerStats("Player 2", "8(2)", 3, 1, 7.0)
        )

        val response = TeamPlayersResponse(
            team = "Barcelona",
            players = players
        )

        assertEquals("Barcelona", response.team)
        assertEquals(2, response.players.size)
        assertEquals("Player 1", response.players[0].name)
        assertEquals("Player 2", response.players[1].name)
    }

    @Test
    fun `test TeamPlayersResponse with empty players list`() {
        val response = TeamPlayersResponse(
            team = "New Team",
            players = emptyList()
        )

        assertEquals("New Team", response.team)
        assertTrue(response.players.isEmpty())
    }

    @Test
    fun `test TeamPlayersResponse copy functionality`() {
        val original = TeamPlayersResponse(
            team = "Real Madrid",
            players = listOf(PlayerStats("Player", "5", 2, 1, 6.5))
        )

        val updated = original.copy(team = "Real Madrid CF")

        assertEquals("Real Madrid CF", updated.team)
        assertEquals(1, updated.players.size)
    }
}

