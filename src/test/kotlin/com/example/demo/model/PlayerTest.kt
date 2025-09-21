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
}